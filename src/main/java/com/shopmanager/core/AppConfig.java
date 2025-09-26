package com.shopmanager.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not load application.properties", e);
        }
    }

    public static String get(String key, String def) {
        return props.getProperty(key, def);
    }

    public static void set(String key, String value) {
        props.setProperty(key, value);
    }

    public static double getDouble(String key, double def) {
        try { return Double.parseDouble(props.getProperty(key)); } catch (Exception e) { return def; }
    }
}
