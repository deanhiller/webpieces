package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.Executor;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.nio.impl.cm.basic.chanimpl.ChannelsFactoryImpl;
import org.webpieces.nio.impl.cm.basic.nioimpl.SelectorProvFactoryImpl;
import org.webpieces.nio.impl.ssl.SslChannelService;
import org.webpieces.nio.impl.threading.ThreadedChannelService;



/**
 * @author Dean Hiller
 */
public class BasChanSvcFactory extends ChannelManagerFactory {
	
	@Override
	public ChannelManager createSingleThreadedChanMgr(String threadName, BufferPool pool) {
		BasChannelService mgr = new BasChannelService(threadName, new ChannelsFactoryImpl(), new SelectorProvFactoryImpl(), pool);
		return new SslChannelService(mgr, pool);
	}

	@Override
	public ChannelManager createMultiThreadedChanMgr(String threadName, BufferPool pool, Executor executor) {
		ChannelManager mgr = createSingleThreadedChanMgr(threadName, pool);
		ThreadedChannelService mgr2 = new ThreadedChannelService(mgr, executor);
		return new SslChannelService(mgr2, pool);
	}
	
}
