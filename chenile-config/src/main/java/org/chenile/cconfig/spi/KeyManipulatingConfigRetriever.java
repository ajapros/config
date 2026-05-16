package org.chenile.cconfig.spi;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.util.ExpressionSupport;
import org.chenile.owiz.Command;

import java.util.List;

/**
 * This needs to be enhanced by all the config retrievers who want to allow manipulating existing keys.
 * They need to return a bunch of CConfigs - each with the intent if manipulating one key (there can be
 * multiple CConfigs that manipulate the same key).
 * <p>The CConfig can replace an entire key or parts of it as specified in the {@link Cconfig#path}</p>
 * The expression language supported is SPEL. Others can also be used.
 */
public abstract class KeyManipulatingConfigRetriever implements Command<ConfigContext> {
    @Override
    public final void execute(ConfigContext configContext) {
        List<Cconfig> cconfigs = retrieveCconfigs(configContext);
        ExpressionSupport.augmentKeys(configContext.allKeys,cconfigs);
    }
    public abstract List<Cconfig> retrieveCconfigs(ConfigContext configContext);
}
