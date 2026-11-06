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
package io.goobi.viewer.modules.heris;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.goobi.viewer.AbstractModuleTest;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.modules.HerisModule;
import io.goobi.viewer.solr.SolrConstants;

class ModuleConfigurationTest extends AbstractModuleTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        AbstractModuleTest.setUpClass();
    }

    /**
     * @see ModuleConfiguration#isModuleEnabled()
     * @verifies return correct value
     */
    @Test
    void isModuleEnabled_shouldReturnCorrectValue() throws Exception {
        Assertions.assertTrue(((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().isModuleEnabled());
    }

    /**
     * @see ModuleConfiguration#getSchemePropertyName()
     * @verifies return correct value
     */
    @Test
    void getSchemePropertyName_shouldReturnCorrectValue() throws Exception {
        assertEquals("PORTAL-SCHEME-TEST",
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getSchemePropertyName());
    }

    /**
     * @see ModuleConfiguration#getAuthorityPropertyName()
     * @verifies return correct value
     */
    @Test
    void getAuthorityPropertyName_shouldReturnCorrectValue() throws Exception {
        assertEquals("PORTAL-AUTHORITY-TEST",
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getAuthorityPropertyName());
    }

    /**
     * @see ModuleConfiguration#getAuthorityPropertyType()
     * @verifies return correct value
     */
    @Test
    void getAuthorityPropertyType_shouldReturnCorrectValue() throws Exception {
        assertEquals("attribute", ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getAuthorityPropertyType());
    }

    /**
     * @see ModuleConfiguration#getAuthorityMapping(String)
     * @verifies return correct value
     */
    @Test
    void getAuthorityMapping_shouldReturnCorrectValue() throws Exception {
        // Mapped value
        assertEquals("bar.example.com",
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getAuthorityMapping("foo.example.com"));
        // Unmapped value
        assertEquals("bla.example.com",
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getAuthorityMapping("bla.example.com"));
    }

    /**
     * @see ModuleConfiguration#getIndexFieldForAuthority(String)
     * @verifies return correct value
     */
    @Test
    void getIndexFieldForAuthority_shouldReturnCorrectValue() throws Exception {
        assertEquals(SolrConstants.PI,
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getIndexFieldForAuthority("foo.example.com"));
    }

    /**
     * @see ModuleConfiguration#getIndexFieldForAuthority(String)
     * @verifies skip patterns without authority attribute
     */
    @Test
    void getIndexFieldForAuthority_shouldSkipPatternsWithoutAuthorityAttribute() throws Exception {
        ModuleConfiguration config = ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration();
        // The test config contains a pattern without an authority attribute before the matching one
        assertEquals(SolrConstants.PI, config.getIndexFieldForAuthority("foo.example.com"));
        assertEquals("", config.getIndexFieldForAuthority("unknown.example.com"));
        assertEquals("", config.getIndexFieldForAuthority(null));
    }

    /**
     * @see ModuleConfiguration#getUrlPatternForAuthority(String)
     * @verifies return correct value
     */
    @Test
    void getUrlPatternForAuthority_shouldReturnCorrectValue() throws Exception {
        assertEquals("{SCHEME}://{HOST}/{ID}",
                ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration().getUrlPatternForAuthority("foo.example.com"));
    }

    /**
     * @see ModuleConfiguration#getUrlPatternForAuthority(String)
     * @verifies skip patterns without authority attribute
     */
    @Test
    void getUrlPatternForAuthority_shouldSkipPatternsWithoutAuthorityAttribute() throws Exception {
        ModuleConfiguration config = ((HerisModule) DataManager.getInstance().getModule(HerisModule.ID)).getConfiguration();
        // The test config contains a pattern without an authority attribute before the matching one
        assertEquals("{SCHEME}://{HOST}/{ID}", config.getUrlPatternForAuthority("foo.example.com"));
        assertEquals("", config.getUrlPatternForAuthority("unknown.example.com"));
        assertEquals("", config.getUrlPatternForAuthority(null));
    }
}
