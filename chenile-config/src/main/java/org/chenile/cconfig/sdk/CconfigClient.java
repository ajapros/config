package org.chenile.cconfig.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chenile.cconfig.sdk.impl.CconfigTypeConverter;

import java.util.Map;

public interface CconfigClient {
    /**
     * @param module - the module under which the key is defined. Modules can be nested.
     * @param key - the key for which value is desired.
     * @return the value for the key.
     * If key is null and only module name is specified, then all keys
     * for module will be returned. Key value can be string or a map.<br/>
     * If a module is specified  and key is null it will be a map with all the keys as keys of the map. The
     * value can be strings or map depending on how the value was defined by any of the Config Retrievers. <br/>
     * Eg:
     * The JSON config retriever defines the keys for a module can look like this:<br/>
     * "key1": "value1"
     * "key2": { "valid JSON": "here", "it": { "can": {"be": { "arbitrarily": "nested"}}} }<br>
     * These JSON values can be over-ridden in two ways : <br/>
     * <ol>
     * <li>They can be over-ridden for all "requests" whether they have a custom attribute or not (__GLOBAL__)</li>
     * <li>They can be over-ridden for specific "requests" that have a custom attribute (eg. all requests that belong to a client "tenant0")</li>
     * </ol>
     * The whole JSON can be over-ridden or specific portions of the JSON (given by path) can be over-ridden. Please see
     * the test cases for more details. <br/>
     * New keys can be introduced by individual config retrievers.
     *
     */
    public Map<String,Object> value(String module, String key);

    default <T> T value(String module, String key, Class<T> targetType) {
        Object rawValue = CconfigTypeConverter.extractRawValue(value(module, key), key);
        return CconfigTypeConverter.convert(rawValue, targetType, module, key);
    }

    default <T> T value(String module, String key, TypeReference<T> targetType) {
        Object rawValue = CconfigTypeConverter.extractRawValue(value(module, key), key);
        return CconfigTypeConverter.convert(rawValue, targetType, module, key);
    }
}
