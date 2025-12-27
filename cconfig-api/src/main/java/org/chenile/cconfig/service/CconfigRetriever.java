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
}
