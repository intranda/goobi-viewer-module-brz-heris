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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.goobi.viewer.AbstractModuleSolrEnabledTest;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.model.security.user.User;
import io.goobi.viewer.model.viewer.StringPair;
import io.goobi.viewer.modules.HerisModule;
import io.goobi.viewer.modules.heris.ModuleConfiguration;

class HerisBeanTest extends AbstractModuleSolrEnabledTest {

    HerisBean bean;

    @BeforeAll
    public static void setUpClass() throws Exception {
        AbstractModuleSolrEnabledTest.setUpClass();
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        bean = new HerisBean();

        FacesContext facesContext = ContextMocker.mockFacesContext();
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
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
        bean.userBean = new UserBean();
        bean.userBean.setUser(new User());
        bean.userBean.getUser().setActive(true);
        assertTrue(bean.userBean.isLoggedIn());

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
        bean.userBean = new UserBean();
        bean.userBean.setUser(new User());
        bean.userBean.getUser().setActive(true);
        assertTrue(bean.userBean.isLoggedIn());

        bean.activeDocumentBean = new ActiveDocumentBean();
        bean.activeDocumentBean.setPersistentIdentifier(PI_KLEIUNIV);
        bean.activeDocumentBean.update();
        assertTrue(bean.activeDocumentBean.isRecordLoaded());

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Mockito.when(request.getHeader("PORTAL-SCHEME")).thenReturn(null);
        Mockito.when(request.getHeader("PORTAL-AUTHORITY")).thenReturn(null);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return empty list if authority param missing
     */
    @Test
    void getExternalLinks_shouldReturnEmptyListIfAuthorityParamMissing() throws Exception {
        bean.userBean = new UserBean();
        bean.userBean.setUser(new User());
        bean.userBean.getUser().setActive(true);
        assertTrue(bean.userBean.isLoggedIn());

        bean.activeDocumentBean = new ActiveDocumentBean();
        bean.activeDocumentBean.setPersistentIdentifier(PI_KLEIUNIV);
        bean.activeDocumentBean.update();
        assertTrue(bean.activeDocumentBean.isRecordLoaded());

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Mockito.when(request.getHeader("PORTAL-SCHEME")).thenReturn("https");
        assertEquals("https", request.getHeader("PORTAL-SCHEME"));
        Mockito.when(request.getHeader("PORTAL-AUTHORITY")).thenReturn(null);

        List<StringPair> ret = bean.getExternalLinks();
        assertTrue(ret.isEmpty());
    }

    /**
     * @see HerisBean#getExternalLinks()
     * @verifies return urls correctly
     */
    @Test
    void getExternalLinks_shouldReturnUrlsCorrectly() throws Exception {
        ModuleConfiguration config = (ModuleConfiguration) DataManager.getInstance().getModule(HerisModule.ID).getConfiguration();
        config.overrideValue("authority[@propertyType]", "header");
        assertEquals("header", config.getAuthorityPropertyType());

        bean.userBean = new UserBean();
        bean.userBean.setUser(new User());
        bean.userBean.getUser().setActive(true);
        assertTrue(bean.userBean.isLoggedIn());

        bean.activeDocumentBean = new ActiveDocumentBean();
        bean.activeDocumentBean.setPersistentIdentifier(PI_KLEIUNIV);
        bean.activeDocumentBean.update();
        assertTrue(bean.activeDocumentBean.isRecordLoaded());

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        Mockito.when(request.getHeader("PORTAL-SCHEME-TEST")).thenReturn("https");
        assertEquals("https", request.getHeader("PORTAL-SCHEME-TEST"));
        Mockito.when(request.getHeader("PORTAL-AUTHORITY-TEST")).thenReturn("foo.example.com");

        List<StringPair> ret = bean.getExternalLinks();
        assertFalse(ret.isEmpty());
        assertEquals("https://bar.example.com/" + PI_KLEIUNIV, ret.get(0).getOne());
        assertEquals("https://bar.example.com/" + PI_KLEIUNIV, ret.get(0).getTwo());
    }
}