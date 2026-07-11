package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.spi.ConfigContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBasedCconfigRetrieverTest {
    @Test
    void loadsTenantSpecificJsonInsteadOfBaseModuleWhenPresent() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "tenant-json", null);

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("tenant-key1", allKeys.get("key1"));
        assertEquals("tenant-value", allKeys.get("tenantKey"));
        assertTrue(allKeys.containsKey("key2"));
    }

    @Test
    void loadsBaseModuleWhenTenantIsMissing() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "missing-tenant", "missing_trajectory");

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("value1", allKeys.get("key1"));
        assertTrue(allKeys.containsKey("key2"));
        assertFalse(allKeys.containsKey("tenantKey"));
    }

    @Test
    void loadsBaseModuleWhenTenantIsNotProvided() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", null, null);

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("value1", allKeys.get("key1"));
        assertTrue(allKeys.containsKey("key2"));
        assertFalse(allKeys.containsKey("tenantKey"));
    }

    @Test
    void mergesBaseCustomAndCustomTrajectoryJsonWhenCustomTrajectoryIsPresent() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "tenant-json", "traj1");

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("tenant-traj-key1", allKeys.get("key1"));
        assertEquals("tenant-value", allKeys.get("tenantKey"));
        assertEquals("trajectory-tenant-value", allKeys.get("trajectoryTenantOnly"));
        Map<String, Object> key2 = castMap(allKeys.get("key2"));
        assertEquals("999", key2.get("abc"));
        Map<String, Object> fields = castMap(key2.get("fields"));
        assertTrue(fields.containsKey("field1"));
        assertTrue(fields.containsKey("field2"));
    }

    @Test
    void mergesBaseCustomAndBaseTrajectoryJsonWhenCustomTrajectoryIsMissing() {
        JsonBasedCconfigRetriever retriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test");
        ConfigContext configContext = new ConfigContext("ctest", "tenant-json", "traj2");

        retriever.execute(configContext);

        Map<String, Object> allKeys = configContext.allKeys;
        assertEquals("trajectory-base-key1", allKeys.get("key1"));
        assertEquals("tenant-value", allKeys.get("tenantKey"));
        assertEquals("trajectory-base-value", allKeys.get("trajectoryBaseOnly"));
        Map<String, Object> key2 = castMap(allKeys.get("key2"));
        Map<String, Object> fields = castMap(key2.get("fields"));
        Map<String, Object> field1 = castMap(fields.get("field1"));
        assertEquals(List.of(5, 500), field1.get("range"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object object) {
        return (Map<String, Object>) object;
    }
}
