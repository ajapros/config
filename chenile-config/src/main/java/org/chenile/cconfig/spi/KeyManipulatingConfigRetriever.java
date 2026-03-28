package org.chenile.cconfig.spi;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.util.ExpressionSupport;
import org.chenile.owiz.Command;

import java.util.List;

public abstract class KeyManipulatingConfigRetriever implements Command<ConfigContext> {
    @Override
    public final void execute(ConfigContext configContext) {
        List<Cconfig> cconfigs = retrieveCconfigs(configContext);
        ExpressionSupport.augmentKeys(configContext.allKeys,cconfigs);
    }
    public abstract List<Cconfig> retrieveCconfigs(ConfigContext configContext);
}
