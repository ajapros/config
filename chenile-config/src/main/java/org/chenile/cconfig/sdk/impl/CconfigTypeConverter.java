package org.chenile.cconfig.sdk.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.ConfigurationException;

import java.util.Map;

public final class CconfigTypeConverter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CconfigTypeConverter() {
    }

    public static Object extractRawValue(Map<String, Object> valueMap, String key) {
        if (key == null || key.isEmpty()) {
            return valueMap;
        }
        return valueMap.get(key);
    }

    public static <T> T convert(Object rawValue, Class<T> targetType, String module, String key) {
        if (rawValue == null) {
            return null;
        }
        if (targetType.isInstance(rawValue)) {
            return targetType.cast(rawValue);
        }
        try {
            if (rawValue instanceof String stringValue && shouldParseAsJson(stringValue, targetType)) {
                return objectMapper.readValue(stringValue, targetType);
            }
            return objectMapper.convertValue(rawValue, targetType);
        } catch (Exception e) {
            throw conversionException(module, key, targetType.getName(), e);
        }
    }

    public static <T> T convert(Object rawValue, TypeReference<T> targetType, String module, String key) {
        if (rawValue == null) {
            return null;
        }
        try {
            if (rawValue instanceof String stringValue && shouldParseAsJson(stringValue)) {
                return objectMapper.readValue(stringValue, targetType);
            }
            return objectMapper.convertValue(rawValue, targetType);
        } catch (Exception e) {
            throw conversionException(module, key, targetType.getType().getTypeName(), e);
        }
    }

    private static boolean shouldParseAsJson(String value, Class<?> targetType) {
        return targetType != String.class && shouldParseAsJson(value);
    }

    private static boolean shouldParseAsJson(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private static ConfigurationException conversionException(String module, String key,
                                                              String targetType, Exception cause) {
        return new ConfigurationException(1706,
                "Cconfig:Cannot convert module %s key %s to %s"
                        .formatted(module, key, targetType), cause);
    }
}
