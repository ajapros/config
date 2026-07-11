package org.chenile.cconfig.util;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public final class ResourceSupport {
    private static final int BASE_PRIORITY = 0;
    private static final int CUSTOM_PRIORITY = 1;
    private static final int TRAJECTORY_PRIORITY = 2;

    private ResourceSupport() {
    }

    public static List<ResolvedResource> resourceLoader(
            String basePath,
            String resourceName,
            String customAttribute,
            String trajectoryId
    ) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ResourceSupport.class.getClassLoader();
        }

        String normalizedBasePath = normalizePath(basePath);
        String normalizedResourceName = normalizePath(resourceName);
        List<ResolvedResource> resolvedResources = new ArrayList<>();

        addResources(classLoader, resolvedResources,
                normalizedBasePath + "/" + normalizedResourceName, BASE_PRIORITY);

        if (customAttribute != null && !customAttribute.isBlank()) {
            addResources(classLoader, resolvedResources,
                    normalizedBasePath + "/" + normalizePath(customAttribute) + "/" + normalizedResourceName,
                    CUSTOM_PRIORITY);
        }

        String trajectoryPath = preferredTrajectoryPath(classLoader, normalizedBasePath, normalizedResourceName,
                customAttribute, trajectoryId);
        if (trajectoryPath != null) {
            addResources(classLoader, resolvedResources, trajectoryPath, TRAJECTORY_PRIORITY);
        }

        resolvedResources.sort(Comparator
                .comparingInt(ResolvedResource::getPriority)
                .thenComparing(ResolvedResource::getLocation));
        return resolvedResources;
    }

    private static void addResources(
            ClassLoader classLoader,
            List<ResolvedResource> resolvedResources,
            String location,
            int priority
    ) {
        try {
            Enumeration<URL> resources = classLoader.getResources(location);
            while (resources.hasMoreElements()) {
                resolvedResources.add(new ResolvedResource(location, priority, resources.nextElement()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read resource " + location, e);
        }
    }

    private static String preferredTrajectoryPath(
            ClassLoader classLoader,
            String normalizedBasePath,
            String normalizedResourceName,
            String customAttribute,
            String trajectoryId
    ) {
        if (trajectoryId == null || trajectoryId.isBlank()) {
            return null;
        }
        String normalizedTrajectoryId = normalizePath(trajectoryId);
        if (customAttribute != null && !customAttribute.isBlank()) {
            String customTrajectoryPath = normalizedBasePath + "/" + normalizePath(customAttribute) + "/"
                    + normalizedTrajectoryId + "/" + normalizedResourceName;
            if (resourceExists(classLoader, customTrajectoryPath)) {
                return customTrajectoryPath;
            }
        }
        String baseTrajectoryPath = normalizedBasePath + "/" + normalizedTrajectoryId + "/" + normalizedResourceName;
        return resourceExists(classLoader, baseTrajectoryPath) ? baseTrajectoryPath : null;
    }

    private static boolean resourceExists(ClassLoader classLoader, String location) {
        try {
            Enumeration<URL> resources = classLoader.getResources(location);
            return resources.hasMoreElements();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot inspect resource " + location, e);
        }
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

    public static final class ResolvedResource {
        private final String location;
        private final int priority;
        private final URL url;

        private ResolvedResource(String location, int priority, URL url) {
            this.location = location;
            this.priority = priority;
            this.url = url;
        }

        public String getLocation() {
            return location;
        }

        public int getPriority() {
            return priority;
        }

        public InputStream openStream() throws Exception {
            return url.openStream();
        }
    }
}
