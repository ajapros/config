package org.chenile.cconfig.sdk.impl;

import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.KeyManipulatingConfigRetriever;
import org.chenile.cconfig.util.ResourceSupport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesBasedCconfigRetriever extends KeyManipulatingConfigRetriever {
    private final String basePath;

    public PropertiesBasedCconfigRetriever(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public List<Cconfig> retrieveCconfigs(ConfigContext configContext) {
        Properties properties = loadProperties(configContext);
        List<Cconfig> cconfigs = new ArrayList<>();
        String modulePrefix = configContext.getModule() + ".";
        for (String propertyName : properties.stringPropertyNames()) {
            if (!propertyName.startsWith(modulePrefix)) {
                continue;
            }
            String remainder = propertyName.substring(modulePrefix.length());
            int firstDot = remainder.indexOf('.');
            Cconfig cconfig = new Cconfig();
            cconfig.moduleName = configContext.getModule();
            cconfig.customAttribute = configContext.getCustomAttribute();
            if (cconfig.customAttribute == null || cconfig.customAttribute.isBlank()) {
                cconfig.customAttribute = Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE;
            }
            if (firstDot < 0) {
                cconfig.keyName = remainder;
            } else {
                cconfig.keyName = remainder.substring(0, firstDot);
                cconfig.path = remainder.substring(firstDot + 1);
            }
            cconfig.avalue = properties.getProperty(propertyName);
            cconfigs.add(cconfig);
        }
        return cconfigs;
    }

    private Properties loadProperties(ConfigContext configContext) {
        String resourceName = configContext.getModule() + ".properties";
        try (InputStream inputStream = ResourceSupport.resourceLoader(basePath, resourceName,
                configContext.getCustomAttribute())) {
            Properties properties = new Properties();
            if (inputStream == null) {
                return properties;
            }
            properties.load(inputStream);
            return properties;
        } catch (Exception e) {
            throw new ConfigurationException(1704,
                    "Cconfig:Cannot read properties resource " + resourceName + " under " + basePath, e);
        }
    }
}
