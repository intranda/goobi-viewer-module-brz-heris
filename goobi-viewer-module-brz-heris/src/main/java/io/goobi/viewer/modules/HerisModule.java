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
package io.goobi.viewer.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.controller.JsonTools;
import io.goobi.viewer.exceptions.IndexAugmenterException;
import io.goobi.viewer.exceptions.ModuleMissingException;
import io.goobi.viewer.model.job.ITaskType;
import io.goobi.viewer.model.security.user.User;
import io.goobi.viewer.modules.heris.ModuleConfiguration;
import io.goobi.viewer.modules.heris.Version;

public class HerisModule implements IModule {

    private static final Logger logger = LogManager.getLogger(HerisModule.class);

    public static final String ID = "viewer-module-heris";
    private static final String NAME = "Goobi Viewer module HERIS";

    private ModuleConfiguration configuration;

    /**
     * Convenience method for retrieving the properly cast instance of this module registered in the core.
     * 
     * @return
     * @throws ModuleMissingException
     */
    public static HerisModule getInstance() throws ModuleMissingException {
        return (HerisModule) DataManager.getInstance().getModule(ID);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return JsonTools.shortFormatVersionString(Version.asJSON());
    }

    @Override
    public String getVersionJson() {
        return Version.asJSON();
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public ModuleConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = new ModuleConfiguration(DataManager.getInstance().getConfiguration().getConfigLocalPath() + "config_" + ID + ".xml");
        }
        return configuration;
    }

    public void injectConfiguration(ModuleConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Map<String, String> getCmsMenuContributions() {
        Map<String, String> map = new HashMap<>();
        // TODO Add CMS menu contributions here
        return map;
    }

    @Override
    public List<String> getSidebarContributions() {
        List<String> ret = new ArrayList<>();
        ret.add("resources/components/widgets/widget_heris_links.xhtml");
        return ret;
    }

    @Override
    public List<String> getAdminContributions() {
        List<String> ret = new ArrayList<>();
        // TODO Add paths to your admin backend contributions here
        return ret;
    }

    /**
     * Widget contributions for regular pages.
     * 
     * @param type <code>PageType</code> name (e.g. viewImage)
     * @return
     */
    @Override
    public List<String> getWidgets(String type) {
        return getConfiguration().getGuiContributions(type);
    }

    @Override
    public List<ITaskType> getTaskTypes() {
        return Collections.emptyList();
    }

    @Override
    public void augmentReIndexRecord(String pi, String dataRepository, String namingScheme) throws IndexAugmenterException {
        // TODO Do something in the module context when exporting record for re-indexing an entire record
    }

    @Override
    public boolean augmentReIndexPage(String pi, int page, SolrDocument doc, String dataRepository, String namingScheme)
            throws IndexAugmenterException {
        // TODO Do something in the module context when exporting record for re-indexing an single page of a record
        return true;
    }

    @Override
    public boolean augmentResetRecord() {
        // TODO Do something in the module context when unloading the current record in <code>NavigationHelper</code>
        return true;
    }

    @Override
    public List<String> getLoginNavigationContributions() {
        return Collections.emptyList();
    }

    @Override
    public int deleteUserContributions(User user) {
        return 0;
    }

    @Override
    public int moveUserContributions(User fromUser, User toUser) {
        return 0;
    }
}
