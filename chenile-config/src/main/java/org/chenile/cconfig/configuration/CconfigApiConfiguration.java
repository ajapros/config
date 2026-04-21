package org.chenile.cconfig.configuration;

import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.sdk.impl.EnvBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.JsonBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.MessageBundleConfigRetriever;
import org.chenile.cconfig.sdk.impl.PropertiesBasedCconfigRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration
public class CconfigApiConfiguration {
    @Value("${chenile.config.path:org/chenile/config}")
    private String configPath;
    @Value("${resource.bundle:messages}")
    private String resourceBundle;
    @Value("${chenile.config.properties.path:${chenile.config.path:org/chenile/config}}")
    private String propertiesPath;
    @Value("${chenile.config.cache-duration:PT5M}")
    private Duration cacheDuration;
    @Bean
    public MemoryCache memoryCache(){
        return new MemoryCache(cacheDuration);
    }

    @Bean
    public JsonBasedCconfigRetriever jsonBasedCconfigRetriever() {
        return new JsonBasedCconfigRetriever(configPath);
    }

    @Bean
    public PropertiesBasedCconfigRetriever propertiesBasedCconfigRetriever() {
        return new PropertiesBasedCconfigRetriever(propertiesPath);
    }

    @Bean
    public EnvBasedCconfigRetriever envBasedCconfigRetriever(Environment environment) {
        return new EnvBasedCconfigRetriever(environment);
    }

    @Bean
    public MessageBundleConfigRetriever messageBundleConfigRetriever() {
        return new MessageBundleConfigRetriever(resourceBundle);
    }
}
