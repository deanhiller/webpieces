package org.playorm.nio.impl.cm.threaded;

import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.StartableExecutorService;



/**
 * @author Dean Hiller
 */
public class ThdChanSvcFactory extends ChannelServiceFactory {

	private ChannelServiceFactory factory;
	
	public ThdChanSvcFactory() {
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManagerFactory#createChannelManager(java.util.Properties)
	 */
	public ChannelService createChannelManager(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("Properties cannot be null");
		Object id = map.get(ChannelManager.KEY_ID);
		Object o = map.get(ChannelManager.KEY_EXECUTORSVC_FACTORY);
		Object b = map.get(ChannelManager.KEY_BUFFER_FACTORY);
		if(id == null)
			throw new IllegalArgumentException("Properties must contain a value for property key=ChannelManagerFactory.KEY_ID");
		else if(o == null)
			throw new IllegalArgumentException("Key=ChannelManager.KEY_EXECUTORSVC_FACTORY must be specified");
		else if(!(o instanceof StartableExecutorService))
			throw new IllegalArgumentException("Key=ChannelManager.KEY_EXECUTORSVC_FACTORY " +
					"must contain an object of type="+StartableExecutorService.class.getName()+", obj="+o);
		else if(b == null)
			throw new IllegalArgumentException("Key=ChannelManager.KEY_BUFFER_FACTORY must be specified");
		else if(!(b instanceof BufferFactory))
			throw new IllegalArgumentException("Key=ChannelManager.KEY_BUFFER_FACTORY must " +
					"contain an object of type="+BufferFactory.class.getName()+", obj="+o);

		BufferFactory bufFactory = (BufferFactory)b;
		StartableExecutorService executor = (StartableExecutorService)o;
		//create a real ChannelManager for the SecureChannelManager to use
		ChannelService mgr = factory.createChannelManager(map);
        SpecialExecutor proxyExecutor = new SpecialExecutor(executor);
		return new ThdChannelService(id, mgr, proxyExecutor, bufFactory);
	}

	@Override
	public void configure(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map cannot be null and must be set");
		Object o = map.get(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY);
		if(o == null || !(o instanceof ChannelServiceFactory))
			throw new IllegalArgumentException("Key=ChannelManagerFactory.KEY_CHILD_CHANNELMGR_FACTORY " +
					"must be set to an instance of ChannelmanagerFactory and wasn't.  your object="+o);
		this.factory = (ChannelServiceFactory)o;
	}
}
