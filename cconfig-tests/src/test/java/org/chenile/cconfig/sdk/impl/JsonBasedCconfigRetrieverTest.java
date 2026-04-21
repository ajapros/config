package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.spi.ConfigContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBasedCconfigRetrieverTest {
    @Test
    void loadsTenantSpecificJsonInsteadOfBaseModuleWhenPresent() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "tenant-json");

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("tenant-value", allKeys.get("tenantKey"));
        assertEquals("tenant-key1", allKeys.get("key1"));
        assertFalse(allKeys.containsKey("key2"));
    }

    @Test
    void loadsBaseModuleWhenTenantIsMissing() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "missing-tenant");

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("value1", allKeys.get("key1"));
        assertTrue(allKeys.containsKey("key2"));
        assertFalse(allKeys.containsKey("tenantKey"));
    }

    @Test
    void loadsBaseModuleWhenTenantIsNotProvided() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", null);

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("value1", allKeys.get("key1"));
        assertTrue(allKeys.containsKey("key2"));
        assertFalse(allKeys.containsKey("tenantKey"));
    }
}
