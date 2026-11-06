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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.exceptions.ModuleMissingException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.viewer.StringPair;
import io.goobi.viewer.modules.HerisModule;
import io.goobi.viewer.modules.heris.ModuleConfiguration;

@Named
@RequestScoped
public class HerisBean implements Serializable {

    private static final long serialVersionUID = 3963436132111065936L;

    private static final Logger logger = LogManager.getLogger(HerisBean.class);

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
     * 
     * @return
     * @should return empty list if user not logged in
     * @should return empty list if no record loaded
     * @should return empty list if scheme param missing
     * @should return empty list if authority param missing
     * @should return urls correctly
     */
    public List<StringPair> getExternalLinks() {
        if (!userBean.isLoggedIn() || !activeDocumentBean.isRecordLoaded()) {
            return Collections.emptyList();
        }

        try {
            ModuleConfiguration config = (ModuleConfiguration) DataManager.getInstance().getModule(HerisModule.ID).getConfiguration();

            String scheme;
            String authority;
            if ("attribute".equals(config.getAuthorityPropertyType())) {
                // Get scheme + authority from session attribute
                scheme = (String) BeanUtils.getSession().getAttribute(config.getSchemePropertyName());
                authority = (String) BeanUtils.getSession().getAttribute(config.getAuthorityPropertyName());
            } else {
                // Get scheme + authority from HTTP header
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                scheme = request.getHeader(config.getSchemePropertyName());
                authority = request.getHeader(config.getAuthorityPropertyName());
            }

            logger.trace("scheme: {}", scheme);
            logger.trace("authority: {}", authority);

            // Use appropriate identifier in URL from configured Solr 
            List<StringPair> ret = new ArrayList<>();
            if (StringUtils.isNotEmpty(scheme) && StringUtils.isNotEmpty(authority)) {
                String field = config.getIndexFieldForAuthority(authority);
                String pattern = config.getUrlPatternForAuthority(authority);
                logger.trace("field: {}", field);
                logger.trace("pattern: {}", pattern);
                if (StringUtils.isNotEmpty(field) && StringUtils.isNotEmpty(pattern)) {
                    String identifier = activeDocumentBean.getViewManager().getTopStructElement().getMetadataValue(field);
                    if (StringUtils.isNotEmpty(identifier)) {
                        String targetHost = config.getAuthorityMapping(authority);
                        String url = pattern.replace("{SCHEME}", scheme).replace("{HOST}", targetHost).replace("{ID}", identifier);
                        ret.add(new StringPair(url, url));
                        logger.trace("added {}", url);
                    }
                } else {
                    logger.warn("HERIS configuration incomplete for authority '{}'. Make sure pattern/text() and pattern/@field are not empty.",
                            authority);
                }

            }

            return ret;
        } catch (ModuleMissingException e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }
}
