package org.chenile.cconfig.sdk.impl;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.core.context.ContextContainer;
import org.chenile.owiz.OrchExecutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of the {@link CconfigClient} interface. This uses an OWIZ
 * orchestration over {@link ConfigContext} to apply multiple config retrievers.
 */
public class CconfigClientImpl implements  CconfigClient{
    private final OrchExecutor<ConfigContext> orchExecutor;
    @Autowired
    MemoryCache memoryCache;
    //@Autowired
   // ContextContainer contextContainer;

    public CconfigClientImpl(OrchExecutor<ConfigContext> orchExecutor) {
        this.orchExecutor = orchExecutor;
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
        try {
            orchExecutor.execute(configContext);
        } catch (Exception e) {
            throw new ConfigurationException(1705,
                    "Cconfig:Cannot execute config orchestration for module " + module, e);
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
        return ContextContainer.getHeader("chenile-tenant-id");
    }
}
