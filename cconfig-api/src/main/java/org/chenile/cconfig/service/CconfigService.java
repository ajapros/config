package org.chenile.cconfig.service;

import org.chenile.cconfig.model.Cconfig;

public interface CconfigService {
    /**
     * @param key - the key for which value is desired.
     * @return the value for the key.
     * Key is expected to contain the module name.
     */
    public Object value(String key);
    public Cconfig save(Cconfig cconfig);
    public Cconfig retrieve(String id);
}
