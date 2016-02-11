package org.playorm.nio.test.tcp;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockNIOServer;

public class TestAsynchWrites extends TestCase {

	private static final Logger log = Logger.getLogger(TestAsynchWrites.class.getName());
	
	private ChannelServiceFactory factory;
	private PacketProcessorFactory procFactory;
	private Settings factoryHolder;
	private BufferFactory bufFactory;
	private InetSocketAddress svrAddr;
	private ChannelService chanMgr;
//	private InetAddress loopBack;
//	private InetSocketAddress loopBackAnyPort;
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);

//	private MockObject mockHandler;
//	private MockObject mockConnect;
//	private TCPChannel client1;
	private MockNIOServer mockServer;
	
//	protected abstract ChannelManagerService getClientChanMgr();
//	protected abstract ChannelManagerService getServerChanMgr();
//	protected abstract Settings getServerFactoryHolder();
//	protected abstract Settings getClientFactoryHolder();	
//	protected abstract String getChannelImplName();
//	protected abstract String getServerChannelImplName();

	public TestAsynchWrites() {
		if(getBufFactory() == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FactoryCreator.KEY_IS_DIRECT, false);
			FactoryCreator creator = FactoryCreator.createFactory(null);
			bufFactory = creator.createBufferFactory(map);
		}
		
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

	protected void setUp() throws Exception {
		HandlerForTests.setupLogging();
		//here I keep using the same channel manager on purpose, just
		//so we get testing between tests that the channel manager shutdown
		//and started back up cleanly.....		
		if(chanMgr == null) {
			chanMgr = getClientChanMgr();
		}
		if(mockServer == null) {
			ChannelService svcChanMgr = getServerChanMgr();
			mockServer = new MockNIOServer(svcChanMgr, getServerFactoryHolder());
		}
		chanMgr.start();		
		svrAddr = mockServer.start();
		log.fine("server port ="+svrAddr);
		
//		loopBack = InetAddress.getByName("127.0.0.1");	
//		loopBackAnyPort = new InetSocketAddress(loopBack, 0);
//		
//		mockHandler = MockObjectFactory.createMock(DataHandler.class);
//		mockHandler.setCloner(new CloneByteBuffer());
//		mockConnect = MockObjectFactory.createMock(ConnectCallback.class);
//		client1 = chanMgr.createTCPChannel("ClientChannel", getClientFactoryHolder());		
	}
	
	protected void tearDown() throws Exception {
		chanMgr.stop();
		chanMgr = null;
		mockServer.stop();
		HandlerForTests.checkForWarnings();
	}	
	
	protected ChannelService getClientChanMgr() {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "[client]");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());

		return factory.createChannelManager(p);
	}

	protected ChannelService getServerChanMgr() {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "[server]");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());	
		return factory.createChannelManager(p);
	}

	protected Settings getClientFactoryHolder() {
		return factoryHolder;
	}
	protected Settings getServerFactoryHolder() {
		return factoryHolder;
	}

	protected String getChannelImplName() {
		return "biz.xsoftware.impl.nio.cm.exception.TCPChannelImpl";
	}

	protected String getServerChannelImplName() {
		return "biz.xsoftware.impl.nio.cm.exception.TCPServerChannelImpl";
	}

	public void testNothing() {
		
	}

	protected BufferFactory getBufFactory() {
		return bufFactory;
	}
	
}
