package org.chenile.cconfig.sdk.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.util.ResourceSupport;
import org.chenile.owiz.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This would typically be the first ConfigRetriever that would be called in the series. <br/>
 * This initialises all the keys for the module.<br/>
 * It will be subsequently enhanced by the other retrievers.
 */
public class JsonBasedCconfigRetriever implements Command<ConfigContext> {
    private static final Logger logger = LoggerFactory.getLogger(JsonBasedCconfigRetriever.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String configPath;

    public JsonBasedCconfigRetriever(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public void execute(ConfigContext configContext) {
        configContext.allKeys = getJsonMapForModuleFromClassPath(configContext);
    }

    private Map<String, Object> getJsonMapForModuleFromClassPath(ConfigContext configContext) {
        List<String> jsonStrings = readModuleAsString(configContext);
        if (jsonStrings.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> merged = new LinkedHashMap<>();
        for (String jsonString : jsonStrings) {
            try {
                Map<String, Object> jsonMap = objectMapper.readValue(jsonString,
                        new TypeReference<LinkedHashMap<String, Object>>() { });
                deepMerge(merged, jsonMap);
            } catch (Exception e) {
                throw new ConfigurationException("1701",
                        "Cconfig:Cannot parse module " + configContext.getModule(), e);
            }
        }
        return merged;
    }

    private List<String> readModuleAsString(ConfigContext configContext) {
        String module = configContext.getModule();
        String resourceName = module;
        String basePath = configPath;
        int lastDot = module.lastIndexOf('.');
        if (lastDot >= 0) {
            basePath = configPath + "/" + module.substring(0, lastDot).replace('.', '/');
            resourceName = module.substring(lastDot + 1);
        }
        logger.debug("Finding the JSON file under {} with resource {}, custom attribute {} and trajectory {}",
                basePath, resourceName + ".json", configContext.getCustomAttribute(), configContext.getTrajectoryId());

        List<String> contents = new java.util.ArrayList<>();
        try {
            for (ResourceSupport.ResolvedResource resource : ResourceSupport.resourceLoader(
                    basePath, resourceName + ".json", configContext.getCustomAttribute(), configContext.getTrajectoryId())) {
                try (InputStream stream = resource.openStream()) {
                    contents.add(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            return contents;
        } catch (Exception e) {
            throw new ConfigurationException("1700", "Cconfig:Cannot read module " + module, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object existing = target.get(entry.getKey());
            Object incoming = entry.getValue();
            if (existing instanceof Map<?, ?> existingMap && incoming instanceof Map<?, ?> incomingMap) {
                Map<String, Object> merged = new LinkedHashMap<>((Map<String, Object>) existingMap);
                deepMerge(merged, (Map<String, Object>) incomingMap);
                target.put(entry.getKey(), merged);
            } else {
                target.put(entry.getKey(), incoming);
            }
        }
    }
}
