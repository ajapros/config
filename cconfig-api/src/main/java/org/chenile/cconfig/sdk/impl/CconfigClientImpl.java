package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.chenile.core.context.ContextContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the {@link CconfigClient} interface. This uses registered
 * {@link CconfigRetriever} instances to get multiple Cconfigs that allow the default value
 * to be mutated.
 */
public class CconfigClientImpl implements  CconfigClient{
    private static final Logger logger = LoggerFactory.getLogger(CconfigClientImpl.class);
    private final CconfigRetrieverFactory cconfigRetrieverFactory;
    @Autowired
    MemoryCache memoryCache;
    @Autowired
    ContextContainer contextContainer;

    public CconfigClientImpl(String configPath, CconfigRetrieverFactory cconfigRetrieverFactory) {
        this.cconfigRetrieverFactory = cconfigRetrieverFactory;
    }

    @Override
    public Map<String,Object> value(String module,String key){
        Map<String,Object> jsonMap  = allKeysForModule(module,customizationAttribute());
        if(key == null || key.isEmpty())
            return jsonMap;
        else if (jsonMap.containsKey(key))
            return new HashMap<>(Map.of(key,jsonMap.get(key)));
        else
            return new HashMap<>();
    }

    /**
     * @param module name of the module
     * @return all the keys in the module. This includes all the keys in the JSON file (if found)
     * and also the DB keys.
     */
    private Map<String,Object> allKeysForModule(String module,String customAttribute) {
        Map<String,Object> allKeys = memoryCache.findJsonMap(module,customAttribute);
        if (allKeys != null) {
            return allKeys;
        }
        ConfigContext configContext = new ConfigContext(module, customAttribute);
        List<CconfigRetriever> retrievers = cconfigRetrieverFactory.getRetrievers();
        if (retrievers == null || retrievers.isEmpty()){
            memoryCache.save(module,customAttribute,allKeys);
            return allKeys;
        }
        for (CconfigRetriever retriever: retrievers){
            retriever.augmentKeys(configContext);
        }
        memoryCache.save(module,customAttribute,configContext.allKeys);
        return configContext.allKeys;
    }

    /**
     * Override this in a subclass to change the default behaviour.
     * @return the value of custom attribute from request headers. Default implementation returns the value of
     * the tenant ID header.
     */
    protected String customizationAttribute(){
        return contextContainer.getHeader("chenile-tenant-id");
    }
}
