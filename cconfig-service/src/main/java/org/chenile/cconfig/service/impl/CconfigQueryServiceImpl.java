package org.chenile.cconfig.service.impl;

import org.chenile.base.exception.NotFoundException;
import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.service.CconfigQueryService;
import org.chenile.cconfig.service.CconfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

public class CconfigQueryServiceImpl implements CconfigQueryService {
    @Autowired
    CconfigRepository cconfigRepository;
    CconfigClient configClient;

    public CconfigQueryServiceImpl(CconfigClient client){
        this.configClient = client;
    }
    @Override
    public Map<String,Object> getAllKeys(String module) {
        return value(module,null);
    }
    @Override
    public Map<String,Object> value(String module,String key) {
        return configClient.value(module,key);
    }

}