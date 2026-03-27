package org.chenile.cconfig.sdk.impl;

import jakarta.annotation.PostConstruct;
import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.chenile.cconfig.spi.ConfigContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class MessageBundleConfigRetriever implements CconfigRetriever {
    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String DEFAULT_LOCALE = "default";
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final String resourceBundle;
    private final CconfigRetrieverFactory cconfigRetrieverFactory;

    public MessageBundleConfigRetriever(String resourceBundle, CconfigRetrieverFactory cconfigRetrieverFactory) {
        this.resourceBundle = resourceBundle;
        this.cconfigRetrieverFactory = cconfigRetrieverFactory;
    }

    @PostConstruct
    public void registerWithFactory() {
        cconfigRetrieverFactory.register(this);
    }

    @Override
    public void augmentKeys(ConfigContext configContext) {
        Map<String, Map<String, String>> bundleValues = readBundleValues(configContext);
        for (Map.Entry<String, Map<String, String>> entry : bundleValues.entrySet()) {
            Object existing = configContext.allKeys.get(entry.getKey());
            if (existing == null) {
                configContext.allKeys.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
                continue;
            }
            if (existing instanceof Map<?,?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingMap = (Map<String, Object>) map;
                for (Map.Entry<String, String> localeEntry : entry.getValue().entrySet()) {
                    existingMap.putIfAbsent(localeEntry.getKey(), localeEntry.getValue());
                }
            }
        }
    }

    @Override
    public int order() {
        return 5;
    }

    private Map<String, Map<String, String>> readBundleValues(ConfigContext configContext) {
        try {
            Resource[] resources = resolver.getResources("classpath*:" + resourceBundle + "*.properties");
            Map<String, Map<String, String>> byKey = new LinkedHashMap<>();
            for (Resource resource : resources) {
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                String locale = localeName(resource);
                mergeProperties(byKey, properties, configContext, locale);
            }
            return byKey;
        } catch (IOException e) {
            throw new ConfigurationException(1703, "Cconfig:Cannot read message bundles " + resourceBundle, e);
        }
    }

    private void mergeProperties(Map<String, Map<String, String>> byKey, Properties properties,
                                 ConfigContext configContext, String locale) {
        String module = configContext.getModule();
        String defaultPrefix = module + ".";
        String globalPrefix = Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE + "." + module + ".";
        String customPrefix = configContext.getCustomAttribute() == null || configContext.getCustomAttribute().isBlank()
                ? null : configContext.getCustomAttribute() + "." + module + ".";
        for (String propertyName : properties.stringPropertyNames()) {
            String key = extractKey(propertyName, defaultPrefix, globalPrefix, customPrefix);
            if (key == null) {
                continue;
            }
            byKey.computeIfAbsent(key, ignored -> new LinkedHashMap<>());
            Map<String, String> localeMap = byKey.get(key);
            String value = properties.getProperty(propertyName);
            if (propertyName.startsWith(defaultPrefix)) {
                localeMap.putIfAbsent(locale, value);
            } else if (propertyName.startsWith(globalPrefix)) {
                localeMap.put(locale, value);
            } else if (customPrefix != null && propertyName.startsWith(customPrefix)) {
                localeMap.put(locale, value);
            }
        }
    }

    private String extractKey(String propertyName, String defaultPrefix, String globalPrefix, String customPrefix) {
        if (customPrefix != null && propertyName.startsWith(customPrefix)) {
            return propertyName.substring(customPrefix.length());
        }
        if (propertyName.startsWith(globalPrefix)) {
            return propertyName.substring(globalPrefix.length());
        }
        if (propertyName.startsWith(defaultPrefix)) {
            return propertyName.substring(defaultPrefix.length());
        }
        return null;
    }

    private String localeName(Resource resource) throws IOException {
        String filename = resource.getFilename();
        if (filename == null || !filename.endsWith(PROPERTIES_SUFFIX)) {
            return DEFAULT_LOCALE;
        }
        String basename = simpleBasename();
        if (!filename.startsWith(basename)) {
            return DEFAULT_LOCALE;
        }
        String suffix = filename.substring(basename.length(), filename.length() - PROPERTIES_SUFFIX.length());
        if (suffix.isEmpty()) {
            return DEFAULT_LOCALE;
        }
        if (suffix.startsWith("_")) {
            suffix = suffix.substring(1);
        }
        return suffix.replace('_', '-');
    }

    private String simpleBasename() {
        int slash = resourceBundle.lastIndexOf('/');
        return slash >= 0 ? resourceBundle.substring(slash + 1) : resourceBundle;
    }
}
