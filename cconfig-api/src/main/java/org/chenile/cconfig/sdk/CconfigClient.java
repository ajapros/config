package org.chenile.cconfig.sdk;

import java.util.Map;

public interface CconfigClient {
    /**
     * @param key - the key for which value is desired.
     * @return the value for the key.
     * Key is of the form {module name}.key. If only module name is specified, then all keys
     * for module will be returned. Key value can be string or a map.
     */
    public Object value(String key);
}
