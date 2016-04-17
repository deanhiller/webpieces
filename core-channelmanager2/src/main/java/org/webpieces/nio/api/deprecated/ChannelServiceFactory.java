package org.webpieces.nio.api.deprecated;

import java.util.Map;
import java.util.Properties;

import org.webpieces.nio.api.libs.BufferHelper;
import org.webpieces.nio.impl.cm.basic.BasChanSvcFactory;


/**
 * There is really two cases for channelmanager
 * 
 * basic -> SSL -> packetizer -> threading -> exception layer
 * basic -> packetizer -> threading -> exception layer
 * 
 * @author Dean Hiller
 */
public abstract class ChannelServiceFactory {

	//a secure nio channel manager reuses the non secure channel manager adding security
	//to that layer....
	public static final String KEY_IMPLEMENTATION_CLASS = "Nio.Implementation";
	public static final String VAL_BASIC_CHANNEL_MGR                 = "org.webpieces.nio.impl.cm.basic.BasChanSvcFactory";

	/**
	 * Key for BufferHelper implementation class not used with channelmanager.
	 */
	public static final String KEY_BUFFER_IMPL = "buffer.impl";
	public static final String VAL_DEFAULT_HELPER = "org.webpieces.nio.impl.cm.basic.BufferHelperImpl";
	
	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaagerFactory
	 * @param map
	 */
	public static ChannelServiceFactory createFactory(Map<String, Object> map) {
		return new BasChanSvcFactory();
	}

	public static BufferHelper bufferHelper(Properties p) {
		String className = VAL_DEFAULT_HELPER;
		if(p != null) {
			String temp = p.getProperty(KEY_BUFFER_IMPL);
			if(temp != null)
				className = temp;
		}
		
		return (BufferHelper)newInstance(className); 
	}	

	private static Object newInstance(String className) {
		Object retVal = null;
		try {
			Class theClass = Class.forName(className);
			retVal = theClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("bug", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("bug", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("bug", e);
		}
		return retVal;
	}
	
	public abstract void configure(Map<String, Object> map);
	
	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaager interface
	 * 
	 * @param map A map containing keys from ChannelManager interface and client's specified values
	 */
	public abstract ChannelService createChannelManager(Map<String, Object> map);
}
