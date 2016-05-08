package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.Executor;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.webpieces.nio.impl.cm.basic.chanimpl.ChannelsFactoryImpl;
import org.webpieces.nio.impl.cm.basic.nioimpl.SelectorProvFactoryImpl;
import org.webpieces.nio.impl.threading.ThreadedChannelService;

import com.webpieces.data.api.BufferPool;



/**
 * @author Dean Hiller
 */
public class BasChanSvcFactory extends ChannelManagerFactory {
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManagerFactory#createChannelManager(java.util.Properties)
	 */
	@Override
	public ChannelManager createSingleThreadedChanMgr(String threadName, BufferPool pool) {
		return new BasChannelService(threadName, new ChannelsFactoryImpl(), new SelectorProvFactoryImpl(), pool);
	}

	@Override
	public ChannelManager createMultiThreadedChanMgr(String threadName, BufferPool pool, Executor executor) {
		ChannelManager mgr = createSingleThreadedChanMgr(threadName, pool);
		return new ThreadedChannelService(mgr, executor);
	}
	
	public ChannelManager createChannelManager(
			String id, BufferPool pool, ChannelsFactory factory, SelectorProviderFactory selectorProvider) {
		return new BasChannelService(id, factory, selectorProvider, pool);
	}
}
