package org.chenile.cconfig.service.impl;

import jakarta.annotation.PostConstruct;
import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.KeyManipulatingConfigRetriever;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DbBasedCconfigRetriever extends KeyManipulatingConfigRetriever {
    @Autowired
    CconfigRepository cconfigRepository;
    private final CconfigRetrieverFactory cconfigRetrieverFactory;

    public DbBasedCconfigRetriever(CconfigRetrieverFactory cconfigRetrieverFactory) {
        this.cconfigRetrieverFactory = cconfigRetrieverFactory;
    }

    @PostConstruct
    public void registerWithFactory() {
        cconfigRetrieverFactory.register(this);
    }

    @Override
    public List<Cconfig> retrieveCconfigs(ConfigContext configContext) {
        List<String> customAttributes = new ArrayList<>();
        customAttributes.add(Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE);
        customAttributes.add(configContext.getCustomAttribute());
        return cconfigRepository.findByModuleNameAndCustomAttributeInOrderByKeyNameAscCustomAttributeAsc(
                configContext.getModule(),customAttributes);
    }
}
