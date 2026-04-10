package org.chenile.cconfig.sdk.cache;

import java.util.Objects;

public class ConfigCacheKey {
    public String module;
    public String customAttribute;

    public ConfigCacheKey(String module, String customAttribute) {
        this.module = module;
        this.customAttribute = customAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigCacheKey that)) return false;
        return Objects.equals(module, that.module)  && Objects.equals(customAttribute, that.customAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, customAttribute);
    }
}
