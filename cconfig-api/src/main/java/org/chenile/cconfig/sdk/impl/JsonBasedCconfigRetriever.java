package org.chenile.cconfig.sdk.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.chenile.cconfig.spi.ConfigContext;
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
public class JsonBasedCconfigRetriever implements CconfigRetriever {
    private static final Logger logger = LoggerFactory.getLogger(JsonBasedCconfigRetriever.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String configPath;
    private final CconfigRetrieverFactory cconfigRetrieverFactory;

    public JsonBasedCconfigRetriever(String configPath, CconfigRetrieverFactory cconfigRetrieverFactory) {
        this.configPath = configPath;
        this.cconfigRetrieverFactory = cconfigRetrieverFactory;
    }

    @PostConstruct
    public void registerWithFactory() {
        cconfigRetrieverFactory.register(this);
    }

    @Override
    public void augmentKeys(ConfigContext configContext) {
        configContext.allKeys = getJsonMapForModuleFromClassPath(configContext.getModule());
    }

    @Override
    public int order() {
        return -1;
    }

    private Map<String, Object> getJsonMapForModuleFromClassPath(String module) {
        String s = readModuleAsString(module);
        if (s == null) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(s, new TypeReference<LinkedHashMap<String, Object>>() { });
        } catch (Exception e) {
            throw new ConfigurationException(1701, "Cconfig:Cannot parse module " + module, e);
        }
    }

    private String readModuleAsString(String module) {
        String modPath = configPath + "/" + module.replaceAll("\\.", "/") + ".json";
        logger.debug("Finding the JSON file for {}", modPath);

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(modPath)) {
            if (stream == null) {
                return null;
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ConfigurationException(1700, "Cconfig:Cannot read module " + module, e);
        }
    }
}
