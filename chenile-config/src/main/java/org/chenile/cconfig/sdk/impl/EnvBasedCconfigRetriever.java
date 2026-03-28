package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.owiz.Command;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * This considers all the module keys and selectively enhances them if there is an environmental override.
 * Note that environment will not add any new config keys. It will only enhance the existing keys.
 * Also, this completely replaces the existing value for the key with a new value rather than selective
 * manipulation of the value of the key.
 */
public class EnvBasedCconfigRetriever implements Command<ConfigContext> {
    private final Environment environment;

    public EnvBasedCconfigRetriever(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void execute(ConfigContext configContext) {
        for (Map.Entry<String,Object> entry : configContext.allKeys.entrySet()) {
            String envValue = environment.getProperty(envKey(configContext.getCustomAttribute(),
                    configContext.getModule(), entry.getKey()));
            if (envValue == null) {
                continue;
            }
            configContext.allKeys.put(entry.getKey(),envValue);
        }
    }
    private String envKey(String customAttribute, String module, String key) {
        return "%s_%s_%s".formatted(customAttribute, module, key);
    }

}
