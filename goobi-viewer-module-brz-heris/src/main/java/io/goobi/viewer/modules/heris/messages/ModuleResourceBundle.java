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
package io.goobi.viewer.modules.heris.messages;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.goobi.viewer.messages.ViewerResourceBundle;

public class ModuleResourceBundle extends ViewerResourceBundle {

    private static final Logger logger = LogManager.getLogger(ModuleResourceBundle.class);

    private static final Object lock = new Object();
    private static final Map<Locale, ResourceBundle> defaultBundles = new ConcurrentHashMap<>();

    /** Resource bundle name. Must be different from the core bundle name (just "messages") to avoid file name collisions. */
    private static final String BUNDLE_NAME = "messages.messages";

    /**
     * Loads default resource bundle if not yet loaded.
     */
    private static synchronized void checkAndLoadDefaultResourceBundle() {
        if (defaultLocale == null) {
            synchronized (lock) {
                if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication() != null) {
                    defaultLocale = FacesContext.getCurrentInstance().getApplication().getDefaultLocale();
                } else {
                    defaultLocale = Locale.ENGLISH;
                }
                checkAndLoadResourceBundles(defaultLocale);
            }
        }
    }

    
    /**
     * Loads resource bundle for the current locale and reloads them if the locale has since changed.
     * 
     * @param inLocale
     * @return The selected locale
     */
    private static Locale checkAndLoadResourceBundles(Locale inLocale) {
        Locale locale;
        if (inLocale != null) {
            locale = inLocale;
        } else if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot() != null) {
            locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        } else {
            locale = Locale.ENGLISH;
        }
        // Reload default bundle if the locale is different
        if (!defaultBundles.containsKey(locale)) {
            synchronized (lock) {
                // Bundle could have been initialized by a different thread in the meanwhile
                if (!defaultBundles.containsKey(locale)) {
                    defaultBundles.put(locale, ResourceBundle.getBundle(BUNDLE_NAME,
                            locale));
                }
            }
        }

        return locale;
    }


    @Override
    protected Object handleGetObject(String key) {
        return getTranslation(key, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    /**
     * 
     * @param text
     * @param locale
     * @return
     */
    public static String getTranslation(String key, Locale locale) {
        // logger.trace("Translation for: {}", text);
        checkAndLoadDefaultResourceBundle();
        locale = checkAndLoadResourceBundles(locale); // If locale is null, the return value will be the current locale
        String value = getTranslation(key, defaultBundles.get(locale), localBundles.get(locale), true);
        // If no translation was found in the resource bundles for the current locale, try the default locale
        if (StringUtils.isEmpty(value) && defaultBundles.containsKey(defaultLocale) && !defaultLocale.equals(locale)) {
            value = getTranslation(key, defaultBundles.get(defaultLocale), localBundles.get(defaultLocale), true);
        }
        if (value == null) {
            value = key;
        }

        return value;
    }
}
