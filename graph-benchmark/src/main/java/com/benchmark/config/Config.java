package com.benchmark.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file. Falling back to system environment variables.");
        }
    }

    public static String get(String key, String defaultValue) {
        String val = null;
        if (dotenv != null) {
            val = dotenv.get(key);
        }
        if (val == null) {
            val = System.getenv(key);
        }
        return val != null ? val : defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }
}
