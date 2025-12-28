package org.chenile.cconfig.service.impl;

import org.chenile.base.exception.NotFoundException;
import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.service.CconfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class CconfigServiceImpl implements CconfigService{
    @Autowired
    CconfigRepository cconfigRepository;

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