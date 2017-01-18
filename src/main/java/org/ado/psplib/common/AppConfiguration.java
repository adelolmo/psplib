package org.ado.psplib.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfiguration {

    private static final File APP_CONFIG_DIRECTORY = new File(FileUtils.getUserDirectory(), ".psplib");
    private static final File CONFIG = new File(APP_CONFIG_DIRECTORY, "psplib.properties");

    private static Properties config;

    public static void setConfigurationProperty(String property, String value) {
        init();
        config.put(property, value);
        store();
    }

    public static void setConfigurationPropertyBoolean(String property, Boolean value) {
        setConfigurationProperty(property, value.toString());
    }

    public static String getConfigurationProperty(String property) {
        init();
        return config.getProperty(property);
    }

    public static boolean getConfigurationPropertyBoolean(String property) {
        return Boolean.valueOf(getConfigurationProperty(property));
    }

    private static void store() {
        try {
            config.store(FileUtils.openOutputStream(CONFIG), "PSP Library Configuration");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save application configuration file", e);
        }
    }

    private static void init() {
        config = loadFileProperties(CONFIG, true);
        if (config.size() > 0) {
            store();
        }
    }

    private static Properties loadFileProperties(File file, boolean createIfNotExist) {
        final Properties prop = new Properties();
        try {
            if (!file.exists()) {
                if (createIfNotExist) {
                    FileUtils.touch(file);
                } else {
                    throw new IOException(String.format("Properties file \"%s\" does not exits.", file.getAbsolutePath()));
                }
            }
            prop.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application configuration file", e);
        }
        return prop;
    }
}
