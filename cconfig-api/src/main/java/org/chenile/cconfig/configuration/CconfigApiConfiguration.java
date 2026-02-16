package org.chenile.cconfig.configuration;

import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CconfigApiConfiguration {
    @Value("${chenile.config.path:org/chenile/config}")
    private String configPath;
    @Bean
    public MemoryCache memoryCache(){
        return new MemoryCache();
    }
}
