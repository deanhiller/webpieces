package org.playorm.nio.test.tcp;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;


public class TestZFailureExceptionCM extends ZNioFailureSuperclass {

	private ChannelServiceFactory factory;
	private PacketProcessorFactory procFactory;
	private Settings factoryHolder;
	
	public TestZFailureExceptionCM(String name) {
		super(name);
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory packetFactory = ChannelServiceFactory.createFactory(props);

		Map<String, Object> props2 = new HashMap<String, Object>();
		props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, packetFactory);
		factory = ChannelServiceFactory.createFactory(props2);	
		FactoryCreator creator = FactoryCreator.createFactory(null);
		procFactory = creator.createPacketProcFactory(null);		
		factoryHolder = new Settings(null, procFactory);
	}
	
	@Override
	protected ChannelService getClientChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "[client]");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		
		return factory.createChannelManager(p);
	}

	@Override
	protected ChannelService getServerChanMgr() {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "[server]");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());		
		return factory.createChannelManager(p);
	}

	@Override
	protected Settings getClientFactoryHolder() {
		return factoryHolder;
	}
	@Override
	protected Settings getServerFactoryHolder() {
		return factoryHolder;
	}	
	@Override
	protected String getChannelImplName() {
		return "org.playorm.nio.impl.cm.exception.ExcTCPChannel";
	}

	@Override
	protected String getServerChannelImplName() {
		return "org.playorm.nio.impl.cm.exception.ExcTCPServerChannel";
	}	
	
//	public void testClientThrowsIntoAcceptHandlerConnect() throws Exception {
//		super.testClientThrowsIntoAcceptHandlerConnect();
//	}
}
