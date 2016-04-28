package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.webpieces.nio.impl.cm.basic.chanimpl.ChannelsFactoryImpl;
import org.webpieces.nio.impl.cm.basic.nioimpl.SelectorProvFactoryImpl;

import com.webpieces.data.api.BufferPool;



/**
 * @author Dean Hiller
 */
public class BasChanSvcFactory extends ChannelManagerFactory {
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManagerFactory#createChannelManager(java.util.Properties)
	 */
	@Override
	public ChannelManager createChannelManager(String id, BufferPool pool) {
		return new BasChannelService(id, new ChannelsFactoryImpl(), new SelectorProvFactoryImpl(), pool);
	}

	public ChannelManager createChannelManager(
			String id, BufferPool pool, ChannelsFactory factory, SelectorProviderFactory selectorProvider) {
		return new BasChannelService(id, factory, selectorProvider, pool);
	}
}
