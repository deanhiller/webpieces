package org.playorm.nio.test.tcp;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;


public class TestZNioSecureCM extends ZNioSuperclassTest {
	
	private ChannelServiceFactory secureFactory;
	private SSLEngineFactory sslEngineFactory;
	private Settings clientFactoryHolder;
	private Settings serverFactoryHolder;
	
	public TestZNioSecureCM() {
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
		
		Map<String, Object> factoryName = new HashMap<String, Object>();
		factoryName.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_SECURE_CHANNEL_MGR);
		factoryName.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory sslLayer = ChannelServiceFactory.createFactory(factoryName);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, sslLayer);
		secureFactory = ChannelServiceFactory.createFactory(props);		
		
		sslEngineFactory = new MockSSLEngineFactory();
		FactoryCreator creator = FactoryCreator.createFactory(null);
		PacketProcessorFactory procFactory = creator.createPacketProcFactory(null);
		clientFactoryHolder = new Settings(sslEngineFactory, procFactory);
		serverFactoryHolder = new Settings(sslEngineFactory, procFactory);
	}

	@Override
	protected ChannelService getClientChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "client");		
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		ChannelService chanMgr = secureFactory.createChannelManager(p);		
		return chanMgr;
	}

	@Override
	protected ChannelService getServerChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "server");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		ChannelService svcChanMgr = secureFactory.createChannelManager(p);
		
		return svcChanMgr;
	}

	@Override
	protected Settings getClientFactoryHolder() {
		return clientFactoryHolder;
	}
	@Override
	protected Settings getServerFactoryHolder() {
		return serverFactoryHolder;
	}
	@Override
	protected String getChannelImplName() {
		return "org.playorm.nio.impl.cm.packet.PacTCPChannel";
	}
	@Override
	protected String getServerChannelImplName() {
		return "org.playorm.nio.impl.cm.packet.PacTCPServerChannel";
	}	
//	public void testHandshakeFailure() {
//		
//	}
//	
//	public void testTooManyBytesGivenFromAppToSSLEngine() {
//		
//	}

	@Override
	public void testConnectClose() throws Exception {
		// TODO Auto-generated method stub
		super.testConnectClose();
	}

	
	
}
