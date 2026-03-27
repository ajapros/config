package org.chenile.cconfig.sdk.impl;

import jakarta.annotation.PostConstruct;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * This considers all the module keys and selectively enhances them if there is an environmental override.
 * Note that environment will not add any new config keys. It will only enhance the existing keys.
 * Also, this completely replaces the existing value for the key with a new value rather than selective
 * manipulation of the value of the key.
 */
public class EnvBasedCconfigRetriever implements CconfigRetriever {
    private final Environment environment;
    private final CconfigRetrieverFactory cconfigRetrieverFactory;

    public EnvBasedCconfigRetriever(Environment environment,
                                    CconfigRetrieverFactory cconfigRetrieverFactory) {
        this.environment = environment;
        this.cconfigRetrieverFactory = cconfigRetrieverFactory;
    }

    @PostConstruct
    public void registerWithFactory() {
        cconfigRetrieverFactory.register(this);
    }

    @Override
    public void augmentKeys(ConfigContext configContext) {
        for (Map.Entry<String,Object> entry : configContext.allKeys.entrySet()) {
            String envValue = environment.getProperty(envKey(configContext.getCustomAttribute(),
                    configContext.getModule(), entry.getKey()));
            if (envValue == null) {
                continue;
            }
            configContext.allKeys.put(entry.getKey(),envValue);
        }
    }

    /**
     * Give it a higher precedence since Environment should ultimately override everything.
     * But we will keep it relatively lower than database since
     * @return
     */
    @Override
    public int order() {
        return 10;
    }

    private String envKey(String customAttribute, String module, String key) {
        return "%s_%s_%s".formatted(customAttribute, module, key);
    }

}
