package org.chenile.cconfig.spi;

import java.util.List;

/**
 * Registry for {@link CconfigRetriever} instances.
 * Retrievers are returned in increasing order of precedence.
 */
public interface CconfigRetrieverFactory {
    void register(CconfigRetriever retriever);

    List<CconfigRetriever> getRetrievers();
}
