package org.chenile.cconfig.service.impl;

import org.chenile.base.exception.NotFoundException;
import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.service.CconfigService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

import java.util.Optional;

public class CconfigServiceImpl implements CconfigService{
    @Autowired
    CconfigRepository cconfigRepository;
    CconfigClient configClient;

    public CconfigServiceImpl(CconfigClient client){
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

    @Override
    public Cconfig save(Cconfig entity) {
        entity = cconfigRepository.save(entity);
        return entity;
    }

    @Override
    public Cconfig retrieve(String id) {
        Optional<Cconfig> entity = cconfigRepository.findById(id);
        if (entity.isPresent()) return entity.get();
        throw new NotFoundException(1500,"Unable to find cconfig with ID " + id);
    }
}