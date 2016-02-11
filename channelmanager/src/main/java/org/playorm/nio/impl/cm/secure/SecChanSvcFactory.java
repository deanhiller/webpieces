package org.playorm.nio.impl.cm.secure;

import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;



/**
 * @author Dean Hiller
 */
public class SecChanSvcFactory extends ChannelServiceFactory {

	private ChannelServiceFactory factory;
	
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
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManagerFactory#createChannelManager(java.util.Properties)
	 */
	@Override
	public ChannelService createChannelManager(Map<String, Object> map) {
		if(map == null)
			throw new IllegalArgumentException("map cannot be null");
		Object theId = map.get(ChannelManager.KEY_ID);
		if(theId == null)
			throw new IllegalArgumentException("map must contain a value for property key=ChannelManager.KEY_ID");
		String id = theId+"";
		//create a real ChannelManager for the SecureChannelManager to use
		ChannelService mgr = factory.createChannelManager(map);
		return new SecChannelService(id, mgr);
	}
}
