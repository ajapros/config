package org.chenile.cconfig.service;

import org.chenile.cconfig.model.Cconfig;

import java.util.Map;

public interface CconfigService {
    public Cconfig save(Cconfig cconfig);
    public Cconfig retrieve(String id);
}
