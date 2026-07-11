package org.chenile.cconfig.spi;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigContext {
    private final String module;
    private final String customAttribute;
    private final String trajectoryId;

    /**
     * The objective is to build this. All the config retrievers must update this in place.
     */
    public  Map<String,Object> allKeys = new LinkedHashMap<>();

    public ConfigContext(String module, String customAttribute,String trajectoryId) {
        this.module = module;
        this.customAttribute = customAttribute;
        this.trajectoryId = trajectoryId;
    }

    public String getModule() {
        return module;
    }

    public String getCustomAttribute() {
        return customAttribute;
    }

    public String getTrajectoryId() {
        return trajectoryId;
    }
}
