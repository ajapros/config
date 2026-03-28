package org.chenile.cconfig.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourceSupportTest {

    @Test
    void loadsCustomizedResourceWhenPresent() throws IOException {
        try (InputStream inputStream = ResourceSupport.resourceLoader("org/chenile/base", "ctest.json", "abc")) {
            assertNotNull(inputStream);
            assertEquals("custom", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim());
        }
    }

    @Test
    void fallsBackToBaseResourceWhenCustomizedResourceIsMissing() throws IOException {
        try (InputStream inputStream = ResourceSupport.resourceLoader("org/chenile/base", "ctest.json", "missing")) {
            assertNotNull(inputStream);
            assertEquals("default", new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim());
        }
    }

    @Test
    void returnsNullWhenNeitherResourceExists() {
        assertNull(ResourceSupport.resourceLoader("org/chenile/base", "missing.json", "abc"));
    }
}
