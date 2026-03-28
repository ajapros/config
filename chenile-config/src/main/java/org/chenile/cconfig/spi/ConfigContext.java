package org.chenile.cconfig.spi;

import org.chenile.cconfig.model.Cconfig;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ConfigContext {
    private final String module;
    private final String customAttribute;
    private final Set<ConfigKeyContext> configKeyContexts = new LinkedHashSet<>();

    /**
     * The objective is to build this. All the config retrievers must update this in place.
     */
    public  Map<String,Object> allKeys = new LinkedHashMap<>();

    public ConfigContext(String module, String customAttribute) {
        this.module = module;
        this.customAttribute = customAttribute;
    }

    public String getModule() {
        return module;
    }

    public String getCustomAttribute() {
        return customAttribute;
    }

    public Set<ConfigKeyContext> getConfigKeyContexts() {
        return configKeyContexts;
    }

    public void addKnownKey(String moduleName, String keyName) {
        addConfigKeyContext(moduleName, keyName, Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE);
        if (customAttribute != null && !customAttribute.isBlank()) {
            addConfigKeyContext(moduleName, keyName, customAttribute);
        }
    }

    public void addKnownKeys(Iterable<Cconfig> configs) {
        for (Cconfig config : configs) {
            if (config.path != null && !config.path.isBlank()) {
                continue;
            }
            addKnownKey(config.moduleName, config.keyName);
        }
    }

    private void addConfigKeyContext(String moduleName, String keyName, String customAttribute) {
        configKeyContexts.add(new ConfigKeyContext(moduleName, keyName, customAttribute));
    }


    public record ConfigKeyContext(String module, String key, String customAttribute) { }
}
