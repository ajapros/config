package org.chenile.cconfig.spi;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.util.ExpressionSupport;

import java.util.List;

public abstract class KeyManipulatingConfigRetriever implements CconfigRetriever{
    @Override
    public final void augmentKeys(ConfigContext configContext) {
        List<Cconfig> cconfigs = retrieveCconfigs(configContext);
        ExpressionSupport.augmentKeys(configContext.allKeys,cconfigs);
    }
    public abstract List<Cconfig> retrieveCconfigs(ConfigContext configContext);
}
