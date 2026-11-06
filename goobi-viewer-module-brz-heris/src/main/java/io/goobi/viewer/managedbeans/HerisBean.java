/**
 * This file is part of the Goobi viewer - a content presentation and management application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goobi.viewer.managedbeans;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.exceptions.ModuleMissingException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.viewer.StringPair;
import io.goobi.viewer.model.viewer.StructElement;
import io.goobi.viewer.modules.HerisModule;
import io.goobi.viewer.modules.heris.ModuleConfiguration;

@Named
@RequestScoped
public class HerisBean implements Serializable {

    private static final long serialVersionUID = 3963436132111065936L;

    private static final Logger logger = LogManager.getLogger(HerisBean.class);

    /** URL schemes that may be used for HERIS links. */
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    @Inject
    ActiveDocumentBean activeDocumentBean;

    @Inject
    UserBean userBean;

    /** Empty constructor. */
    public HerisBean() {
        // Empty
    }

    @PostConstruct
    public void init() {
        //
    }

    /**
     * 
     * @return
     * @throws ModuleMissingException
     * @should return correct value
     */
    public boolean isModuleEnabled() throws ModuleMissingException {
        return HerisModule.getInstance().getConfiguration().isModuleEnabled();
    }

    /**
     * Builds the HERIS link(s) for the currently loaded record from the scheme and authority values supplied by the portal (as HTTP headers or
     * session attributes, depending on configuration).
     *
     * @return List of URL/label pairs; empty list if no link can be built
     * @should return empty list if module disabled
     * @should return empty list if user not logged in
     * @should return empty list if no record loaded
     * @should return empty list if scheme param missing
     * @should return empty list if authority param missing
     * @should return empty list if session missing in attribute mode
     * @should return empty list if scheme not http or https
     * @should return empty list if top struct element cannot be loaded
     * @should return urls correctly
     * @should return urls correctly from session attributes
     * @should url encode identifier
     */
    public List<StringPair> getExternalLinks() {
        if (!userBean.isLoggedIn() || !activeDocumentBean.isRecordLoaded()) {
            return Collections.emptyList();
        }

        ModuleConfiguration config;
        try {
            config = HerisModule.getInstance().getConfiguration();
        } catch (ModuleMissingException e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
        if (!config.isModuleEnabled()) {
            return Collections.emptyList();
        }

        String scheme;
        String authority;
        if ("attribute".equals(config.getAuthorityPropertyType())) {
            // Get scheme + authority from session attribute
            HttpSession session = BeanUtils.getSession();
            if (session == null) {
                logger.trace("No HTTP session available.");
                return Collections.emptyList();
            }
            scheme = getStringAttribute(session, config.getSchemePropertyName());
            authority = getStringAttribute(session, config.getAuthorityPropertyName());
        } else {
            // Get scheme + authority from HTTP header
            HttpServletRequest request = BeanUtils.getRequest();
            if (request == null) {
                logger.trace("No HTTP request available.");
                return Collections.emptyList();
            }
            scheme = request.getHeader(config.getSchemePropertyName());
            authority = request.getHeader(config.getAuthorityPropertyName());
        }

        logger.trace("scheme: {}", scheme);
        logger.trace("authority: {}", authority);

        if (StringUtils.isEmpty(scheme) || StringUtils.isEmpty(authority)) {
            return Collections.emptyList();
        }
        // The scheme is inserted verbatim into a link href, so only allow web schemes (prevents e.g. javascript: URLs)
        if (!ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            logger.warn("Unsupported HERIS URL scheme '{}', only http and https are allowed.", scheme);
            return Collections.emptyList();
        }

        // Use appropriate identifier in URL from configured Solr field
        String field = config.getIndexFieldForAuthority(authority);
        String pattern = config.getUrlPatternForAuthority(authority);
        logger.trace("field: {}", field);
        logger.trace("pattern: {}", pattern);
        if (StringUtils.isEmpty(field) || StringUtils.isEmpty(pattern)) {
            logger.warn("HERIS configuration incomplete for authority '{}'. Make sure pattern/text() and pattern/@field are not empty.",
                    authority);
            return Collections.emptyList();
        }

        StructElement topStructElement = activeDocumentBean.getViewManager().getTopStructElement();
        if (topStructElement == null) {
            logger.warn("Top structure element could not be loaded, cannot build HERIS link.");
            return Collections.emptyList();
        }
        String identifier = topStructElement.getMetadataValue(field);
        if (StringUtils.isEmpty(identifier)) {
            return Collections.emptyList();
        }

        String targetHost = config.getAuthorityMapping(authority);
        String encodedIdentifier = URLEncoder.encode(identifier, StandardCharsets.UTF_8).replace("+", "%20");
        String url = pattern.replace("{SCHEME}", scheme).replace("{HOST}", targetHost).replace("{ID}", encodedIdentifier);
        logger.trace("added {}", url);

        List<StringPair> ret = new ArrayList<>(1);
        ret.add(new StringPair(url, url));
        return ret;
    }

    /**
     *
     * @param session HTTP session
     * @param name Attribute name
     * @return Attribute value if it is a {@link String}; null otherwise
     */
    private static String getStringAttribute(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof String s ? s : null;
    }
}
