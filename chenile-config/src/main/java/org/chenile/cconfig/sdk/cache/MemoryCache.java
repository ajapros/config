package org.chenile.cconfig.sdk.cache;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for resolved config payloads with TTL-based eviction on read.
 */
public class MemoryCache {
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final Map<ConfigCacheKey, CacheEntry> configMap = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public MemoryCache() {
        this(DEFAULT_TTL);
    }

    public MemoryCache(Duration ttl) {
        Duration effectiveTtl = (ttl == null) ? DEFAULT_TTL : ttl;
        this.ttlMillis = Math.max(0, effectiveTtl.toMillis());
    }

    public void save(String module,String customAttribute, Map<String,Object> jsonMap){
        configMap.put(new ConfigCacheKey(module,customAttribute),new CacheEntry(jsonMap, System.currentTimeMillis()));
    }

    public Map<String,Object> findJsonMap(String module, String customAttribute) {
        ConfigCacheKey ckey = new ConfigCacheKey(module,customAttribute);
        CacheEntry entry = configMap.get(ckey);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            configMap.remove(ckey, entry);
            return null;
        }
        return entry.jsonMap;
    }

    private boolean isExpired(CacheEntry entry) {
        if (ttlMillis == 0) {
            return true;
        }
        return System.currentTimeMillis() - entry.createdAtMillis >= ttlMillis;
    }

    private record CacheEntry(Map<String,Object> jsonMap, long createdAtMillis) {
    }
}
