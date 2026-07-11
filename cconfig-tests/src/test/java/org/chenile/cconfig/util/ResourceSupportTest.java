package org.chenile.cconfig.util;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceSupportTest {

    @Test
    void loadsCustomizedResourceWhenPresent() {
        assertEquals(List.of("default", "custom"),
                readAll("org/chenile/base", "ctest.json", "abc", null));
    }

    @Test
    void prefersCustomTrajectoryResourceOverBaseTrajectoryResourceWhenPresent() {
        assertEquals(List.of("default", "custom", "trajectory-custom"),
                readAll("org/chenile/base", "ctest.json", "abc", "traj1"));
    }

    @Test
    void fallsBackToBaseTrajectoryResourceWhenCustomTrajectoryResourceIsMissing() {
        assertEquals(List.of("default", "custom", "trajectory-base"),
                readAll("org/chenile/base", "ctest.json", "abc", "traj2"));
    }

    @Test
    void returnsEmptyListWhenNeitherResourceExists() {
        List<ResourceSupport.ResolvedResource> resources = ResourceSupport.resourceLoader(
                "org/chenile/base", "missing.json", "abc", "traj1");
        assertTrue(resources.isEmpty());
    }

    private List<String> readAll(String basePath, String resourceName, String customAttribute, String trajectoryId) {
        return ResourceSupport.resourceLoader(basePath, resourceName, customAttribute, trajectoryId)
                .stream()
                .map(resource -> {
                    try (InputStream inputStream = resource.openStream()) {
                        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
