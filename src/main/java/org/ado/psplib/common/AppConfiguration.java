package org.ado.psplib.common;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfiguration {

    private static final File APP_CONFIG_DIRECTORY = new File(FileUtils.getUserDirectory(), ".psplib");
    private static final File CONFIG = new File(APP_CONFIG_DIRECTORY, "psplib.properties");

    private static Properties config;

    public static void setConfiguration(String property, String value) {
        init();
        config.put(property, value);
        store();
    }

    public static void setConfigurationBoolean(String property, Boolean value) {
        setConfiguration(property, value.toString());
    }

    public static String getConfiguration(String property) {
        init();
        return config.getProperty(property);
    }

    public static boolean getConfigurationBoolean(String property) {
        return Boolean.parseBoolean(getConfiguration(property));
    }

    private static void store() {
        try {
            config.store(FileUtils.openOutputStream(CONFIG), "PSP Library Configuration");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save application configuration file", e);
        }
    }

    private static void init() {
        config = loadFileProperties(CONFIG);
        if (config.size() > 0) {
            store();
        }
    }

    private static Properties loadFileProperties(File file) {
        final Properties prop = new Properties();
        InputStream inStream = null;
        try {
            FileUtils.touch(file);
            inStream = new FileInputStream(file);
            prop.load(inStream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application configuration file", e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return prop;
    }
}
