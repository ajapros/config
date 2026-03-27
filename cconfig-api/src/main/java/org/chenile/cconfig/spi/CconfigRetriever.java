package org.chenile.cconfig.spi;

/**
 * This is implemented by all the classes that retrieve configs from various sources. They either add keys to
 * the context or change the existing keys. (or do both)
 */
public interface CconfigRetriever {
    void augmentKeys(ConfigContext configContext);
    // increasing order of precedence. All ConfigRetrievers will be chosen from the lowest to the highest.
    // Higher precedence ones can modify the values set by the ones with lower precedence
    // by default it will have the lowest order.
    default int order() {
        return 0;
    }
}
