package org.chenile.cconfig.sdk;

import java.util.Map;

public interface CconfigClient {
    /**
     * @param key - the key for which value is desired.
     * @return the value for the key.
     * Key is of the form {module name}.key. If only module name is specified, then all keys
     * for module will be returned. Key value can be string or a map.<br/>
     * If a module is specified it will be a map with all the keys as keys of the map. The
     * value can be strings or map depending on how the value was defined in the JSON file
     * and all the DB records. <br/>
     * Eg:
     * The JSON file that defines the keys for a module can look like this:<br/>
     * "key1": "value1"
     * "key2": { "valid JSON": "here", "it": { "can": {"be": { "arbitrarily": "nested"}}} }<br>
     * Modules are defined in JSON files under a config path defined in "chenile.config.path". For example
     * module1 is defined in module1.json directly under "chenile.config.path". <br/>
     * These JSON values can be over-ridden in two ways in the database: <br/>
     * <ol>
     * <li>They can be over-ridden for all "requests" whether they have a custom attribute or not (__GLOBAL__)</li>
     * <li>They can be over-ridden for specific "requests" that have a custom attribute (eg. all requests that belong to a client "tenant0")</li>
     * </ol>
     * The whole JSON can be over-ridden or specific portions of the JSON (given by path) can be over-ridden. Please see
     * the test cases for more details. <br/>
     * It is possible that no JSON file exists. All keys can be defined in the database directly without using the JSON.
     * In that case, keys can only be defined using path as null. Subsequent records can override this record.
     * It is assumed that DB records don't alter the same path. If they do so, the result is indeterminate.
     *
     */
    public Object value(String key);
}
