package org.playorm.nio.impl.cm.basic.nioimpl;

import java.nio.channels.spi.SelectorProvider;

import org.playorm.nio.api.testutil.nioapi.Select;
import org.playorm.nio.api.testutil.nioapi.SelectorProviderFactory;


/**
 */
public class SelectorProvFactoryImpl implements SelectorProviderFactory
{

    /**
     */
    public Select provider(String id)
    {
        SelectorProvider provider = SelectorProvider.provider();
        return new SelectorImpl(id, provider);
    }

}
