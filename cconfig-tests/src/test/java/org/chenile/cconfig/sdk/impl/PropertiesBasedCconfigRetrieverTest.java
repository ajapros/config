package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.ConfigContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesBasedCconfigRetrieverTest {
    @Test
    void loadsCustomizedPropertiesWhenPresent() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "abc"));

        assertEquals(2, cconfigs.size());
        Map<String, Cconfig> byKey = cconfigs.stream().collect(Collectors.toMap(cc -> cc.keyName, Function.identity()));
        assertEquals("custom", byKey.get("k1").avalue);
        assertEquals("extra", byKey.get("k3").avalue);
        assertEquals("path.to.value", byKey.get("k3").path);
    }

    @Test
    void fallsBackToBasePropertiesWhenCustomizedResourceIsMissing() {
        PropertiesBasedCconfigRetriever retriever = new PropertiesBasedCconfigRetriever(
                "org/chenile/cconfig");

        List<Cconfig> cconfigs = retriever.retrieveCconfigs(new ConfigContext("m1", "missing"));

        assertEquals(2, cconfigs.size());
        Map<String, Cconfig> byKey = cconfigs.stream().collect(Collectors.toMap(cc -> cc.keyName, Function.identity()));
        assertEquals("base", byKey.get("k1").avalue);
        assertEquals("path.to.value", byKey.get("k2").path);
        assertEquals("base-path", byKey.get("k2").avalue);
    }
}
