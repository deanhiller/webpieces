package org.playorm.nio.api.libs;

import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.deprecated.ChannelManager;



public abstract class FactoryCreator {

	public static final String KEY_IMPLEMENTATION_CLASS = "ByteBuffer.Implementation";	
	public static final String VAL_DEFAULT_FACTORY = "org.playorm.nio.impl.libs.FactoryCreatorImpl";
	//public static final String VAL_HEADER_IMPL = "biz.xsoftware.impl.nio.ChanMgrFactoryImpl";
	
	public static final String KEY_IS_DIRECT = "key.is.direct";
	public static final String KEY_PACKET_SEPARATOR = "key.packet.separator";
	public static final String KEY_NUM_THREADS = "key.num.threads";
	public static final String KEY_ID = ChannelManager.KEY_ID;
	public static final String KEY_SSL_ENGINE = "key.ssl.engine";
	
	public static FactoryCreator createFactory(Map<String, Object> map) {
		String className = VAL_DEFAULT_FACTORY;
		if(map != null) {
			String temp = (String)map.get(KEY_IMPLEMENTATION_CLASS);
			if(temp != null)
				className = temp;
		}

		FactoryCreator retVal = null;
		try {
//			FactoryCreator.class.getClassLoader().
			Class<? extends FactoryCreator> theClass = Class.forName(className).asSubclass(FactoryCreator.class);
			retVal = theClass.newInstance();
			retVal.configure(map);
//			Class theClass = Class.forName(className);
//			retVal = (PacketProcessorFactory)theClass.newInstance();
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
	
	public abstract BufferFactory createBufferFactory(Map<String, Object> map);
	
	public abstract PacketProcessorFactory createPacketProcFactory(Map<String, Object> map);
	
	public abstract StartableExecutorService createExecSvcFactory(Map<String, Object> map);
	
    public abstract StartableExecutorService createAdvancedExecSvc(Map<String, Object> map);
    
    public abstract StartableRouterExecutor createRoutingExecutor(String id, int numThreads);
    
	public abstract AsyncSSLEngine createSSLEngine(Object id, SSLEngine engine, Map<String, Object> newParam);
	
	public abstract ChannelSession createSession(RegisterableChannel channel);
}
