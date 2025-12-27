package org.chenile.cconfig.service.impl;

import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.service.CconfigRetriever;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DbBasedCconfigRetriever implements CconfigRetriever {
    @Autowired
    CconfigRepository cconfigRepository;

    @Override
    public List<Cconfig> findAllKeysForModule(String module, String customAttribute) {
        List<String> customAttributes = new ArrayList<>();
        customAttributes.add(Cconfig.GLOBAL_CUSTOMIZATION_ATTRIBUTE);
        customAttributes.add(customAttribute);
        return cconfigRepository.findByModuleNameAndCustomAttributeInOrderByKeyNameAscCustomAttributeAsc(module,customAttributes);
    }
}
