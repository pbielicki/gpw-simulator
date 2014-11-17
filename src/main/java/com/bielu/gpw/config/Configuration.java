package com.bielu.gpw.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Configuration {

    private static final String CONFIGURATION_FILE = "/gpw-configuration.xml";
    private static final Log LOG = LogFactory.getLog(Configuration.class);
    private final List<MenuItemInfo> menuItemList;
    private final List<GuiPluginInfo> guiPluginList;
    private final XMLConfiguration config;
    private final String recommendationsFile;
    private final String quotesUrl;

    private static final Configuration CONFIGURATION = new Configuration();

    public static Configuration getInstance() {
        return CONFIGURATION;
    }

    public List<MenuItemInfo> getMenuItemList() {
        return Collections.unmodifiableList(menuItemList);
    }

    public List<GuiPluginInfo> getGuiPluginList() {
        return Collections.unmodifiableList(guiPluginList);
    }

    public String getRecommendationsFile() {
        return recommendationsFile;
    }

    public String getQuotesUrl() {
        return quotesUrl;
    }

    private Configuration() {
        config = new XMLConfiguration();
        try {
            URL url = Class.class.getResource(CONFIGURATION_FILE);
            if (url == null) {
                LOG.error(String.format("Unable to locate %s file with configuration", CONFIGURATION_FILE));
                throw new IllegalStateException(
                        String.format("Unable to locate %s file with configuration", CONFIGURATION_FILE));
            }
            config.load(Class.class.getResourceAsStream(CONFIGURATION_FILE));
            config.setExpressionEngine(new XPathExpressionEngine());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize configuration", e);
        }

        menuItemList = configureMenuItems();
        guiPluginList = configureGuiPlugins();
        recommendationsFile = config.getString("recommendation/@fileName");
        quotesUrl = config.getString("quotes/@url");

        config.clear();
    }

    private List<GuiPluginInfo> configureGuiPlugins() {
        List<GuiPluginInfo> result = new ArrayList<GuiPluginInfo>();
        int i = 0;
        while (true) {
            i++;
            String prefix = String.format("gui/plugin[@order='%d']/", i);
            String label = config.getString(String.format("%s%s", prefix, "@label"));
            String className = config.getString(String.format("%s%s", prefix, "@pluginClass"));
            if (label == null) {
                break;
            }
            result.add(new GuiPluginInfo(label, className));
        }
        return Collections.unmodifiableList(result);
    }

    private List<MenuItemInfo> configureMenuItems() {
        List<MenuItemInfo> result = new ArrayList<MenuItemInfo>();
        int i = 0;
        while (true) {
            i++;
            String prefix = String.format("tray/menuItem[@order='%d']/", i);
            String type = config.getString(String.format("%s%s", prefix, "@type"));
            String label = config.getString(String.format("%s%s", prefix, "@label"));
            String className = config.getString(String.format("%s%s", prefix, "@listenerClass"));
            if (type == null) {
                break;
            }
            result.add(new MenuItemInfo(type, label, className));
        }
        return Collections.unmodifiableList(result);
    }
}
