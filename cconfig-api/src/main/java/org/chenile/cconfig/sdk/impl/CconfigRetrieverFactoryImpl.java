package org.chenile.cconfig.sdk.impl;

import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.CconfigRetrieverFactory;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CconfigRetrieverFactoryImpl implements CconfigRetrieverFactory {
    private final CopyOnWriteArrayList<CconfigRetriever> retrievers = new CopyOnWriteArrayList<>();

    @Override
    public void register(CconfigRetriever retriever) {
        if (retriever == null || retrievers.contains(retriever)) {
            return;
        }
        retrievers.add(retriever);
    }

    @Override
    public List<CconfigRetriever> getRetrievers() {
        return retrievers.stream()
                .sorted(Comparator.comparingInt(CconfigRetriever::order))
                .toList();
    }
}
