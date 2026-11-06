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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.goobi.viewer.AbstractModuleSolrEnabledTest;
import io.goobi.viewer.AbstractModuleTest;
import io.goobi.viewer.model.security.user.User;
import io.goobi.viewer.model.viewer.StringPair;
import io.goobi.viewer.model.viewer.StructElement;
import io.goobi.viewer.model.viewer.ViewManager;
import io.goobi.viewer.modules.HerisModule;
import io.goobi.viewer.modules.heris.ModuleConfiguration;
import io.goobi.viewer.solr.SolrConstants;

class HerisBeanTest extends AbstractModuleSolrEnabledTest {

    private static final String SCHEME_PROPERTY = "PORTAL-SCHEME-TEST";
    private static final String AUTHORITY_PROPERTY = "PORTAL-AUTHORITY-TEST";
    private static final String AUTHORITY = "foo.example.com";

    private HerisBean bean;
    private ModuleConfiguration config;
    private HttpServletRequest request;

    @BeforeAll
    public static void setUpClass() throws Exception {
        AbstractModuleSolrEnabledTest.setUpClass();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Fresh module config for each test so that overridden values don't leak into other tests
        config = new ModuleConfiguration(AbstractModuleTest.TEST_CONFIG_PATH);
        HerisModule.getInstance().injectConfiguration(config);

        bean = new HerisBean();

        FacesContext facesContext = ContextMocker.mockFacesContext();
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(externalContext.getRequest()).thenReturn(request);
    }

    /**
     * @see HerisBean#isModuleEnabled()
     * @verifies return correct value
     */
    @Test
    void isModuleEnabled_shouldReturnCorrectValue() throws Exception {
        assertTrue(bean.isModuleEnabled());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if module disabled
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfModuleDisabled() throws Exception {
        useHeaderMode();
        loginUser();
        mockLoadedRecord("PPN123");
        mockHeaders("https", AUTHORITY);
        assertFalse(bean.getExternalLinks().isEmpty());

        config.overrideValue("enabled", false);
        assertTrue(bean.getExternalLinks().isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if user not logged in
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfUserNotLoggedIn() throws Exception {
        bean.userBean = new UserBean();
        assertFalse(bean.userBean.isLoggedIn());

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if no record loaded
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfNoRecordLoaded() throws Exception {
        loginUser();
        bean.activeDocumentBean = new ActiveDocumentBean();
        assertFalse(bean.activeDocumentBean.isRecordLoaded());

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if scheme param missing
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfSchemeParamMissing() throws Exception {
        useHeaderMode();
        loginUser();
        loadRecord();
        mockHeaders(null, AUTHORITY);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if authority param missing
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfAuthorityParamMissing() throws Exception {
        useHeaderMode();
        loginUser();
        loadRecord();
        mockHeaders("https", null);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if session missing in attribute mode
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfSessionMissingInAttributeMode() throws Exception {
        assertEquals("attribute", config.getAuthorityPropertyType());
        loginUser();
        mockLoadedRecord("PPN123");
        Mockito.when(request.getSession(false)).thenReturn(null);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if scheme not http or https
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfSchemeNotHttpOrHttps() throws Exception {
        useHeaderMode();
        loginUser();
        mockLoadedRecord("PPN123");

        mockHeaders("javascript:alert(1)//", AUTHORITY);
        assertTrue(bean.getExternalLinks().isEmpty());

        mockHeaders("ftp", AUTHORITY);
        assertTrue(bean.getExternalLinks().isEmpty());

        // Scheme check is case-insensitive
        mockHeaders("HTTP", AUTHORITY);
        assertFalse(bean.getExternalLinks().isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if top struct element cannot be loaded
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfTopStructElementCannotBeLoaded() throws Exception {
        useHeaderMode();
        loginUser();
        mockLoadedRecord(null);
        mockHeaders("https", AUTHORITY);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return urls correctly
     */
    @Test
    void getExternalLinks_shouldReturnUrlsCorrectly() throws Exception {
        useHeaderMode();
        loginUser();
        loadRecord();
        mockHeaders("https", AUTHORITY);

        List<StringPair> ret = bean.getExternalLinks();
        assertEquals(1, ret.size());
        assertEquals("https://bar.example.com/" + PI_KLEIUNIV, ret.get(0).getOne());
        assertEquals("https://bar.example.com/" + PI_KLEIUNIV, ret.get(0).getTwo());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return urls correctly from session attributes
     */
    @Test
    void getExternalLinks_shouldReturnUrlsCorrectlyFromSessionAttributes() throws Exception {
        assertEquals("attribute", config.getAuthorityPropertyType());
        loginUser();
        mockLoadedRecord("PPN123");
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(request.getSession(false)).thenReturn(session);
        Mockito.when(session.getAttribute(SCHEME_PROPERTY)).thenReturn("https");
        Mockito.when(session.getAttribute(AUTHORITY_PROPERTY)).thenReturn(AUTHORITY);

        List<StringPair> ret = bean.getExternalLinks();
        assertEquals(1, ret.size());
        assertEquals("https://bar.example.com/PPN123", ret.get(0).getOne());

        // Non-String attribute values are treated as missing
        Mockito.when(session.getAttribute(AUTHORITY_PROPERTY)).thenReturn(Integer.valueOf(1));
        assertTrue(bean.getExternalLinks().isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies url encode identifier
     */
    @Test
    void getExternalLinks_shouldUrlEncodeIdentifier() throws Exception {
        useHeaderMode();
        loginUser();
        mockLoadedRecord("a b/c?d");
        mockHeaders("https", AUTHORITY);

        List<StringPair> ret = bean.getExternalLinks();
        assertEquals(1, ret.size());
        assertEquals("https://bar.example.com/a%20b%2Fc%3Fd", ret.get(0).getOne());
    }

    private void useHeaderMode() {
        config.overrideValue("authority[@propertyType]", "header");
        assertEquals("header", config.getAuthorityPropertyType());
    }

    private void loginUser() {
        bean.userBean = new UserBean();
        bean.userBean.setUser(new User());
        bean.userBean.getUser().setActive(true);
        assertTrue(bean.userBean.isLoggedIn());
    }

    /**
     * Loads {@link #PI_KLEIUNIV} from the test Solr index.
     */
    private void loadRecord() throws Exception {
        bean.activeDocumentBean = new ActiveDocumentBean();
        bean.activeDocumentBean.setPersistentIdentifier(PI_KLEIUNIV);
        bean.activeDocumentBean.update();
        assertTrue(bean.activeDocumentBean.isRecordLoaded());
    }

    /**
     * Mocks a loaded record whose top struct element has the given PI.
     *
     * @param pi PI value of the top struct element; if null, the top struct element cannot be loaded
     */
    private void mockLoadedRecord(String pi) {
        ViewManager viewManager = Mockito.mock(ViewManager.class);
        if (pi != null) {
            StructElement topStructElement = Mockito.mock(StructElement.class);
            Mockito.when(topStructElement.getMetadataValue(SolrConstants.PI)).thenReturn(pi);
            Mockito.when(viewManager.getTopStructElement()).thenReturn(topStructElement);
        }
        bean.activeDocumentBean = Mockito.mock(ActiveDocumentBean.class);
        Mockito.when(bean.activeDocumentBean.isRecordLoaded()).thenReturn(true);
        Mockito.when(bean.activeDocumentBean.getViewManager()).thenReturn(viewManager);
    }

    private void mockHeaders(String scheme, String authority) {
        Mockito.when(request.getHeader(SCHEME_PROPERTY)).thenReturn(scheme);
        Mockito.when(request.getHeader(AUTHORITY_PROPERTY)).thenReturn(authority);
    }
}
