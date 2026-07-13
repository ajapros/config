package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.owiz.Command;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * This considers all the module keys and selectively enhances them if there is an environmental override.
 * Note that environment will not add any new config keys. It will only enhance the existing keys.
 * Also, this completely replaces the existing value for the key with a new value rather than selective
 * manipulation of the value of the key.<br/>
 * You can provide environment level overrides at a customAttribute level by prefixing the environment key
 * with the customAttribute name. But it is not possible to provide a trajectory level override at the
 * environment. This is not a bad practice since it cannot accommodate for runtime changes.
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
