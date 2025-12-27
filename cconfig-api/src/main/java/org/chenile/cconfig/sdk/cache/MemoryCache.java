package org.chenile.cconfig.sdk.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a hashmap as a read through cache.
 * Replace with proper cache with TTL if it is desired to change the attributes frequently
 */
public class MemoryCache {
    private final Map<CconfigKey,Map<String,Object>> configMap = new HashMap<>();

    public void save(String module,String customAttribute, Map<String,Object> jsonMap){
        configMap.put(new CconfigKey(module,customAttribute),jsonMap);
    }

    public Map<String,Object> findJsonMap(String module, String customAttribute) {
        CconfigKey ckey = new CconfigKey(module,customAttribute);
        return configMap.get(ckey);
    }
}
