package org.webpieces.nio.impl.cm.basic.nioimpl;

import java.nio.channels.spi.SelectorProvider;

import org.webpieces.nio.api.testutil.nioapi.Select;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;


/**
 */
public class SelectorProvFactoryImpl implements SelectorProviderFactory
{

    /**
     */
    public Select provider()
    {
        SelectorProvider provider = SelectorProvider.provider();
        return new SelectorImpl(provider);
    }

}
