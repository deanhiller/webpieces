package org.webpieces.nio.api;

import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.impl.cm.basic.BasChanSvcFactory;
import org.webpieces.nio.impl.jdk.JdkSelectorImpl;


/**
 * @author Dean Hiller
 */
public abstract class ChannelManagerFactory {

	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaagerFactory
	 */
	public static ChannelManagerFactory createFactory() {
        SelectorProvider provider = SelectorProvider.provider();
        JdkSelectorImpl selector = new JdkSelectorImpl(provider);
		return createFactory(selector);
	}
	
	public static ChannelManagerFactory createFactory(JdkSelect apis) {
		return new BasChanSvcFactory(apis);
	}
	
	/**
	 * Creates a single threaded ChannelManager. 
	 * 
	 * @param id
	 * @param pool
	 * @return
	 */
	public abstract ChannelManager createSingleThreadedChanMgr(String id, BufferPool pool);
	
	/**
	 * Creates a multi-threaded ChannelManager where data from any one channel will run IN-ORDER on the 
	 * thread pool you give us.  We use a trick such that all data still comes in the pieces read off
	 * the socket but the SessionExecutor layer ensures data enters your thread pool in order while not allowing
	 * one channel to starve one thread....That makes it sound like it would get out of order, but the details
	 * are encapsulated in SessionExecutorImpl if you would like to read that class to see how it is done.  
	 * 
	 * @param id
	 * @param pool
	 * @param executor
	 * @return
	 */
	public abstract ChannelManager createMultiThreadedChanMgr(String id, BufferPool pool, Executor executor);
}
