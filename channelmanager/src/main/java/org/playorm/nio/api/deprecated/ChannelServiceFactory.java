package org.playorm.nio.api.deprecated;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.StartableExecutorService;
import org.playorm.nio.api.libs.StartableRouterExecutor;


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
	public static final String VAL_BASIC_CHANNEL_MGR                 = "org.playorm.nio.impl.cm.basic.BasChanSvcFactory";	
	public static final String VAL_SECURE_CHANNEL_MGR          = "org.playorm.nio.impl.cm.secure.SecChanSvcFactory";
	public static final String VAL_PACKET_CHANNEL_MGR          = "org.playorm.nio.impl.cm.packet.PacChanSvcFactory";
	public static final Object VAL_EXCEPTION_CHANNEL_MGR       = "org.playorm.nio.impl.cm.exception.ExcChanSvcFactory";	
	public static final String VAL_THREAD_CHANNEL_MGR          = "org.playorm.nio.impl.cm.threaded.ThdChanSvcFactory";
	public static final String VAL_DENIALOFSERVICE_CHANNEL_MGR = "org.playorm.nio.impl.cm.dos.ChanSvcFactoryImpl";	
	public static final String VAL_REGISTER_FOR_READ_MGR       = "org.playorm.nio.impl.cm.readreg.RegChanSvcFactory";
	public static final String VAL_ROUTING_EXEC_MGR            = "org.playorm.nio.impl.cm.routing.ThdChanSvcFactory";
	
	/**
	 * Key specific to all ChannelManagers except basic
	 */
	public static final String KEY_CHILD_CHANNELMGR_FACTORY = "Nio.Secure.Child.ChannelManager";	

	/**
	 * Key for BufferHelper implementation class not used with channelmanager.
	 */
	public static final String KEY_BUFFER_IMPL = "buffer.impl";
	public static final String VAL_DEFAULT_HELPER = "org.playorm.nio.impl.cm.basic.BufferHelperImpl";
	
	private static ChannelService chanMgr;
	
	public static synchronized ChannelService getSingleton() {
		if(chanMgr == null)
			chanMgr = createDefaultChannelMgr("Singleton");
		return chanMgr;
	}
	
	/**
	 * Creates a ChannelManager/Service with these layers installed
	 * 
	 * From bottom to top layer as data flows up from socket to client...
	 * nio abstraction -> ssl layer -> packetizing layer -> threading -> exception catching
	 * 
	 * All implement the same interface.
	 * 
	 * @param id
	 * @return
	 */
	public static ChannelService createDefaultChannelMgr(Object id) {
		FactoryCreator creator = FactoryCreator.createFactory(null);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		map.put(FactoryCreator.KEY_NUM_THREADS, 10);
		
		BufferFactory bufferFactory = creator.createBufferFactory(map);
		StartableExecutorService execSvcFactory = creator.createExecSvcFactory(map);
		StartableRouterExecutor executorSvc = creator.createRoutingExecutor("rawPool", 20);
		
		ChannelServiceFactory factory = createDefaultStack();
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelManager.KEY_ID, id);
		props.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, execSvcFactory);
		props.put(ChannelManager.KEY_ROUTINGEXECUTORSVC_FACTORY, executorSvc);
		props.put(ChannelManager.KEY_BUFFER_FACTORY, bufferFactory);
		ChannelService mgr = factory.createChannelManager(props);
		
		return mgr;
	}

	public static ChannelService createNewChannelManager(String id) {
		FactoryCreator creator = FactoryCreator.createFactory(null);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		map.put(FactoryCreator.KEY_NUM_THREADS, 10);
		
		BufferFactory bufferFactory = creator.createBufferFactory(map);
		StartableExecutorService executorSvc = creator.createExecSvcFactory(map);
		
		ChannelServiceFactory factory = createNewStack();
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelManager.KEY_ID, id);
		props.put(ChannelManager.KEY_BUFFER_FACTORY, bufferFactory);
		props.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, executorSvc);
		return factory.createChannelManager(props);
	}
	
	/**
	 * 
	 * 
	 * @param id
	 * @return
	 */
	public static ChannelService createRawChannelManager(String id) {
		FactoryCreator creator = FactoryCreator.createFactory(null);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		BufferFactory bufferFactory = creator.createBufferFactory(map);
		StartableRouterExecutor executorSvc = creator.createRoutingExecutor("rawPool", 20);
		
		ChannelServiceFactory factory = createRawStack();
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelManager.KEY_ID, id);
		props.put(ChannelManager.KEY_BUFFER_FACTORY, bufferFactory);
		props.put(ChannelManager.KEY_ROUTINGEXECUTORSVC_FACTORY, executorSvc);
		return factory.createChannelManager(props);
	}

	private static ChannelServiceFactory createNewStack() {
		//LAYER 1: basic
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
		
		//LAYER 2: threadpool
		Map<String, Object> props3 = new HashMap<String, Object>();
		props3.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_THREAD_CHANNEL_MGR);
		props3.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory threadedFactory = ChannelServiceFactory.createFactory(props3);
		
		//LAYER 3: secure
		Map<String, Object> props2 = new HashMap<String, Object>();
		props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_SECURE_CHANNEL_MGR);
		props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, threadedFactory);
		ChannelServiceFactory secureFactory = ChannelServiceFactory.createFactory(props2);
		
		//Layer 4: catch log exceptions thrown from the clients listeners
		Map<String, Object> props4 = new HashMap<String, Object>();
		props4.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props4.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, secureFactory);
		ChannelServiceFactory excFactory = ChannelServiceFactory.createFactory(props4);
		
		return excFactory;		
	}
	
	private static ChannelServiceFactory createRawStack() {
		//LAYER 1: basic
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);

		//LAYER 2: Ensures that registerForReads/unregisterForReads lifecycle is independent of connect/disconnect
		//         for tcp AND udp
		Map<String, Object> props0 = new HashMap<String, Object>();
		props0.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_REGISTER_FOR_READ_MGR);
		props0.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory regReads = ChannelServiceFactory.createFactory(props0);
		
		//LAYER 3: special executor so channels are routed to their threads so ordering stays the same and you can
		//do decryption, or reform the packet before handing off to a threadpool that does nto map channel to thread
		Map<String, Object> props1 = new HashMap<String, Object>();
		props1.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_ROUTING_EXEC_MGR);
		props1.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, regReads);
		ChannelServiceFactory routing = ChannelServiceFactory.createFactory(props1);
		
		//Layer 4: catch log exceptions thrown from the clients listeners
		Map<String, Object> props4 = new HashMap<String, Object>();
		props4.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props4.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, routing);
		ChannelServiceFactory excFactory = ChannelServiceFactory.createFactory(props4);
		
		return excFactory;		
	}
	/**
	 * This should be a private method and will be in the future so please
	 * don't use this one.
	 * 
	 * Creates the Default Stack for you with basic, packetizer, secure, threadpool, and 
	 * exception protection
	 */
	@Deprecated
	public static ChannelServiceFactory createDefaultStack() {
		
		ChannelServiceFactory regReads = createRawStack();
		
		//LAYER 3: secure
		Map<String, Object> props2 = new HashMap<String, Object>();
		props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_SECURE_CHANNEL_MGR);
		props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, regReads);
		ChannelServiceFactory secureFactory = ChannelServiceFactory.createFactory(props2);

		//LAYER 4: packetizer
		Map<String, Object> props1 = new HashMap<String, Object>();
		props1.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props1.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, secureFactory);
		ChannelServiceFactory cmProcFactory = ChannelServiceFactory.createFactory(props1);
		
		//LAYER 5: threadpool
//		Map<String, Object> props3 = new HashMap<String, Object>();
//		props3.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_THREAD_CHANNEL_MGR);
//		props3.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, cmProcFactory);
//		ChannelServiceFactory threadedFactory = ChannelServiceFactory.createFactory(props3);
		
		//LAYER 6: exception protection
		Map<String, Object> props4 = new HashMap<String, Object>();
		props4.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props4.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, cmProcFactory);
		ChannelServiceFactory excFactory = ChannelServiceFactory.createFactory(props4);
		
		return excFactory;
	}
	
	/**
	 * All Keys(and some values) to put in the map variable can be found 
	 * as the constants in ChannelManaagerFactory
	 * @param map
	 */
	public static ChannelServiceFactory createFactory(Map<String, Object> map) {
		String className = VAL_BASIC_CHANNEL_MGR;
		if(map != null) {
			Object tmp = map.get(KEY_IMPLEMENTATION_CLASS);
			if(!(tmp instanceof String))
				throw new IllegalArgumentException(
						"key=ChannelManagerFactory.KEY_IMPLEMENTATION_CLASS must be of type String and wasn't");
			if(tmp != null)
				className = (String)tmp;
		}
		
		ChannelServiceFactory factory = (ChannelServiceFactory)newInstance(className); 
		factory.configure(map);
		return factory;
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
