package org.chenile.cconfig.util;

import java.io.InputStream;

public final class ResourceSupport {
    private ResourceSupport() {
    }

    public static InputStream resourceLoader(String basePath, String resourceName, String customAttribute) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ResourceSupport.class.getClassLoader();
        }

        String normalizedBasePath = normalizePath(basePath);
        String normalizedResourceName = normalizePath(resourceName);
        if (customAttribute != null && !customAttribute.isBlank()) {
            InputStream customStream = classLoader.getResourceAsStream(
                    normalizedBasePath + "/" + customAttribute + "/" + normalizedResourceName);
            if (customStream != null) {
                return customStream;
            }
        }
        return classLoader.getResourceAsStream(normalizedBasePath + "/" + normalizedResourceName);
    }

    private static String normalizePath(String value) {
        String normalized = value == null ? "" : value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
