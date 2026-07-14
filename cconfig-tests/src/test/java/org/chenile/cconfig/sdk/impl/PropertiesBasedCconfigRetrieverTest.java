package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.util.ExpressionSupport;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesBasedCconfigRetrieverTest {
    @Test
    void loadsCustomizedPropertiesWhenPresent() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "abc", "missing"));

        assertEquals(9, cconfigs.size());
        Map<String, Cconfig> byPathlessKey = cconfigs.stream()
                .filter(cc -> cc.path == null || cc.path.isEmpty())
                .collect(Collectors.toMap(cc -> cc.keyName + "-" + cc.avalue, Function.identity()));
        assertTrueValue(byPathlessKey.containsKey("k1-base"));
        assertTrueValue(byPathlessKey.containsKey("k1-custom"));
    }

    @Test
    void fallsBackToBasePropertiesWhenCustomizedResourceIsMissing() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "missing", "missing"));

        assertEquals(5, cconfigs.size());
        Map<String, Cconfig> byKey = cconfigs.stream().collect(Collectors.toMap(cc -> cc.keyName, Function.identity()));
        assertEquals("base", byKey.get("k1").avalue);
        assertEquals("path.to.value", byKey.get("k2").path);
        assertEquals("base-path", byKey.get("k2").avalue);
    }

    @Test
    void layersCustomTrajectoryPropertiesAheadOfBaseAndCustomResources() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "abc", "traj1"));

        assertEquals(13, cconfigs.size());
        Map<String, Object> keys = new LinkedHashMap<>();
        keys.put("k1", "seed");
        ExpressionSupport.augmentKeys(keys, cconfigs);
        assertEquals("trajectory-custom", keys.get("k1"));
        assertEquals("trajectory-custom-extra", keys.get("k4"));
    }

    @Test
    void layersBaseTrajectoryPropertiesWhenCustomTrajectoryPropertiesAreMissing() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "abc", "traj2"));

        assertEquals(11, cconfigs.size());
        Map<String, Object> keys = new LinkedHashMap<>();
        keys.put("k1", "seed");
        ExpressionSupport.augmentKeys(keys, cconfigs);
        assertEquals("trajectory-base", keys.get("k1"));
        assertEquals("trajectory-base-extra", keys.get("k4"));
    }

    @Test
    void layersTrajectoryPropertiesAheadOfBaseAndCustomWithAllLayersTested() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");
        ConfigContext context = new ConfigContext("m1","abc","traj1");
        retriever.execute(context);
        assertEquals("traj1_value",context.allKeys.get("k88"));
        context = new ConfigContext("m1","abc","missing");
        retriever.execute(context);
        assertEquals("abc_value",context.allKeys.get("k88"));
        context = new ConfigContext("m1","missing","missing");
        retriever.execute(context);
        assertEquals("base_value",context.allKeys.get("k88"));
    }

    @Test
    void mergesBaseAndCustomTrajectoryPropertyFilesWhenBothExist() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");
        ConfigContext context = new ConfigContext("m1","z_tenant","traj8");
        retriever.execute(context);
        @SuppressWarnings("unchecked")
        Map<String, Object> k22 = (Map<String, Object>) context.allKeys.get("k22");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) k22.get("fields");
        assertEquals("ztenant_f1", fields.get(0).get("name"));
        assertEquals("ztenant_traj8_value", fields.get(0).get("value"));
        assertEquals("base_traj8_marker", context.allKeys.get("k23"));
    }

    private void assertTrueValue(boolean value) {
        assertEquals(true, value);
    }
}
