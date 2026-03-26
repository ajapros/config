package org.chenile.cconfig.service;

import org.chenile.cconfig.model.Cconfig;

import java.util.List;

public interface CconfigRetriever {
    /**
     * Find all the Cconfigs that match module and customAttribute.
     * This will also return all the common customAttribute value (__GLOBAL__)
     * This will return the __GLOBAL__ first followed by the passed customization attribute
     * @param module the module
     * @param customAttribute the customAttribute
     * @return all values that match the combination and also for __GLOBAL__
     */
    List<Cconfig> findAllKeysForModule(String module,String customAttribute);
    // increasing order of precedence. All ConfigRetrievers will be chosen from the lowest to the highest.
    // Higher precedence ones can modify the values set by the ones with lower precedence
    // by default it will have the lowest order.
    default int order() {
        return 0;
    }
}
