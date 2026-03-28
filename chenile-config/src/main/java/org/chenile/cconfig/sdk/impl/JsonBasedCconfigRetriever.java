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
        String s = readModuleAsString(configContext);
        if (s == null) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(s, new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (Exception e) {
            throw new ConfigurationException(1701, "Cconfig:Cannot parse module " + configContext.getModule(), e);
        }
    }

    private String readModuleAsString(ConfigContext configContext) {
        String module = configContext.getModule();
        String resourceName = module;
        String basePath = configPath;
        int lastDot = module.lastIndexOf('.');
        if (lastDot >= 0) {
            basePath = configPath + "/" + module.substring(0, lastDot).replace('.', '/');
            resourceName = module.substring(lastDot + 1);
        }
        logger.debug("Finding the JSON file under {} with resource {} and custom attribute {}",
                basePath, resourceName + ".json", configContext.getCustomAttribute());

        try (InputStream stream = ResourceSupport.resourceLoader(basePath, resourceName + ".json",
                configContext.getCustomAttribute())) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ConfigurationException(1700, "Cconfig:Cannot read module " + module, e);
        }
    }
}
