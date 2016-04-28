package org.webpieces.nio.api;

import org.webpieces.nio.impl.cm.basic.BasChanSvcFactory;

import com.webpieces.data.api.BufferPool;


/**
 * @author Dean Hiller
 */
public abstract class ChannelManagerFactory {

	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaagerFactory
	 * @param map
	 */
	public static ChannelManagerFactory createFactory() {
		return new BasChanSvcFactory();
	}
	
	public abstract ChannelManager createChannelManager(String id, BufferPool pool);
}
