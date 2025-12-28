package org.chenile.cconfig.service;

import org.chenile.cconfig.model.Cconfig;

import java.util.Map;

public interface CconfigService {
    /**
     * @param module - the module
     * @return all the keys for the module
     */
    public Map<String,Object> getAllKeys(String module);
    /**
     * @param module - the module under which the key is defined.
     * @param key - the key for which value is desired.
     * @return the value for the key returned as a map. The key will be the solitary key in the map.
     */
    public Map<String,Object> value(String module,String key);
    public Cconfig save(Cconfig cconfig);
    public Cconfig retrieve(String id);
}
