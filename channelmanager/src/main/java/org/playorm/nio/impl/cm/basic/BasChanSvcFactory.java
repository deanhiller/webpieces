package org.playorm.nio.impl.cm.basic;

import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.testutil.chanapi.ChannelsFactory;
import org.playorm.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.playorm.nio.impl.cm.basic.chanimpl.ChannelsFactoryImpl;
import org.playorm.nio.impl.cm.basic.nioimpl.SelectorProvFactoryImpl;



/**
 * @author Dean Hiller
 */
public class BasChanSvcFactory extends ChannelServiceFactory {

	@Override
	public void configure(Map<String, Object> props) {

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
		Object o = map.get(ChannelManager.KEY_BUFFER_FACTORY);
		if(o == null || !(o instanceof BufferFactory))
			throw new IllegalArgumentException("Key=ChannelManager.KEY_BUFFER_FACTORY must " +
					"not be null and must contain an instance of ByteBufferFactory");
        ChannelsFactory factory;
        if(map.get("mock.channelsFactory") == null) {
            factory = new ChannelsFactoryImpl();            
        } else {
            factory = (ChannelsFactory)map.get("mock.channelsFactory");
        }
        
        SelectorProviderFactory mgr;
        if(map.get("mock.selectorProvider") == null) {
            mgr = new SelectorProvFactoryImpl();
        } else {
            mgr = (SelectorProviderFactory)map.get("mock.selectorProvider");
        }

		return new BasChannelService(id, factory, mgr, (BufferFactory)o);
	}


}
