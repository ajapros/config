package org.chenile.cconfig.sdk.cache;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemoryCacheTest {

    @Test
    void cacheLookupIncludesTrajectoryId() {
        MemoryCache memoryCache = new MemoryCache();
        Map<String, Object> value = Map.of("k1", "v1");

        memoryCache.save("m1", "tenant0", "traj1", value);

        assertEquals(value, memoryCache.findJsonMap("m1", "tenant0", "traj1"));
        assertNull(memoryCache.findJsonMap("m1", "tenant0", "traj2"));
    }
}
