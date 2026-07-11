package org.chenile.cconfig.service.impl;

import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.service.CconfigQueryService;

import java.util.Map;

public class CconfigQueryServiceImpl implements CconfigQueryService {
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