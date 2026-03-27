package org.chenile.cconfig.configuration;

import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.chenile.cconfig.sdk.impl.EnvBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.MessageBundleConfigRetriever;
import org.chenile.cconfig.sdk.impl.CconfigRetrieverFactoryImpl;
import org.chenile.cconfig.sdk.impl.JsonBasedCconfigRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CconfigApiConfiguration {
    @Value("${chenile.config.path:org/chenile/config}")
    private String configPath;
    @Value("${resource.bundle:messages}")
    private String resourceBundle;
    @Bean
    public MemoryCache memoryCache(){
        return new MemoryCache();
    }

    @Bean
    public CconfigRetrieverFactory cconfigRetrieverFactory() {
        return new CconfigRetrieverFactoryImpl();
    }

    @Bean
    public JsonBasedCconfigRetriever jsonBasedCconfigRetriever(CconfigRetrieverFactory cconfigRetrieverFactory) {
        return new JsonBasedCconfigRetriever(configPath, cconfigRetrieverFactory);
    }

    @Bean
    public EnvBasedCconfigRetriever envBasedCconfigRetriever(Environment environment,
                                                             CconfigRetrieverFactory cconfigRetrieverFactory) {
        return new EnvBasedCconfigRetriever(environment, cconfigRetrieverFactory);
    }

    @Bean
    public MessageBundleConfigRetriever messageBundleConfigRetriever(CconfigRetrieverFactory cconfigRetrieverFactory) {
        return new MessageBundleConfigRetriever(resourceBundle, cconfigRetrieverFactory);
    }
}
