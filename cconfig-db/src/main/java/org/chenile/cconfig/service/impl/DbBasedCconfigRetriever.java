package org.chenile.cconfig.service.impl;

import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.KeyManipulatingConfigRetriever;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DbBasedCconfigRetriever extends KeyManipulatingConfigRetriever {
    @Autowired
    CconfigRepository cconfigRepository;

    /**
     *
     * @param configContext - the context for retrieving
     * @return all the cconfigs that either belong to the custom attribute and trajectory ID or which are global.
     */
    @Override
    public List<Cconfig> retrieveCconfigs(ConfigContext configContext) {
        List<String> customAttributes = new ArrayList<>();
        List<String> trajectoryIds = new ArrayList<>();
        customAttributes.add(Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE);
        customAttributes.add(configContext.getCustomAttribute());
        trajectoryIds.add(Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE);
        trajectoryIds.add(configContext.getTrajectoryId());
        return cconfigRepository.findByModuleNameAndCustomAttributeInAndTrajectoryIdInOrderByKeyNameAscCustomAttributeAsc(
                configContext.getModule(),customAttributes,trajectoryIds);
    }
}