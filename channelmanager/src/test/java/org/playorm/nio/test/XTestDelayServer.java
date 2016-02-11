package org.playorm.nio.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public class XTestDelayServer extends TestCase {

	private static final Logger log = Logger.getLogger(XTestDelayServer.class.getName());
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	private ChannelServiceFactory factory;
	private BufferFactory bufFactory;
	private ChannelService chanMgr;
	
	private InetSocketAddress delaySvrAddr;
	private DelayServer delayServer;
	private EchoServer echoServer;

	private MockObject mockHandler;
	private MockObject mockConnect;
	private PacketProcessorFactory procFactory;
	private Settings factoryHolder;
	
	public XTestDelayServer(String arg0) {
		super(arg0);
		if(bufFactory == null) {
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
	}

	protected void setUp() throws Exception {
		FactoryCreator creator = FactoryCreator.createFactory(null);
		procFactory = creator.createPacketProcFactory(null);		
		factoryHolder = new Settings(null, procFactory);
		
		HandlerForTests.setupLogging();
		Logger.getLogger("").setLevel(Level.INFO);
		//here I keep using the same channel manager on purpose, just
		//so we get testing between tests that the channel manager shutdown
		//and started back up cleanly.....		
		if(chanMgr == null) {
			chanMgr = createClientChanMgr("[client]");
		}
		if(echoServer == null) {
			ChannelService svrChanMgr = createSvrChanMgr("[echoServer]");
			echoServer = new EchoServer(svrChanMgr, factoryHolder);
		}
		if(delayServer == null) {
			delayServer = new DelayServer();
		}
		chanMgr.start();
		InetSocketAddress echoSvrAddr = echoServer.start();
		log.fine("echo server port ="+echoSvrAddr);
		delaySvrAddr = delayServer.start(echoSvrAddr);
		log.fine("delay server port ="+delaySvrAddr);
		
		mockHandler = MockObjectFactory.createMock(DataListener.class);
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		mockConnect = MockObjectFactory.createMock(ConnectionCallback.class);
	}
	
	protected void tearDown() throws Exception {
		chanMgr.stop();
		chanMgr = null;
		delayServer.stop();
		echoServer.stop();
		HandlerForTests.checkForWarnings();
		Logger.getLogger("").setLevel(Level.FINEST);
	}
	
	protected ChannelService createClientChanMgr(String name) {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, name);
		p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
		
		return factory.createChannelManager(p);
	}

	protected ChannelService createSvrChanMgr(String name) {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, name);
		p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);		
		return factory.createChannelManager(p);
	}
	
	public void testVerySmallReadWrite() throws Exception {
		ByteBuffer b = ByteBuffer.allocate(4000);
		
		log.info("getting all proper connections");
		int size = 40;
		String[] methodNames = new String[size];
		for(int i = 0; i < size; i++) {
			methodNames[i] = "connected";
		}
		TCPChannel[] clients = new TCPChannel[size];	
		for(int i = 0; i < size; i++) {
			clients[i] = chanMgr.createTCPChannel("Client["+i+"]", factoryHolder);			
			log.fine("starting connect");
			Thread.sleep(100);
			clients[i].oldConnect(delaySvrAddr, (ConnectionCallback)mockConnect);
		}
		mockConnect.expect(methodNames);
		log.info("done getting all connections");
		
		for(TCPChannel client : clients) {
			client.registerForReads((DataListener)mockHandler);
		}
		
		int numWrites = 200;
		String payload = "hello";
		HELPER.putString(b, payload);
		HELPER.doneFillingBuffer(b);
		methodNames = new String[size*numWrites];
		for(int i = 0; i < size*numWrites; i++) {
			methodNames[i] = "incomingData";
		}		
		
		PerfTimer timer = new PerfTimer();
		PerfTimer timer2 = new PerfTimer();
		timer.start();
		timer2.start();
		for(TCPChannel client : clients) {
			for(int i = 0; i < numWrites; i++) {
				client.oldWrite(b);
				b.rewind();
			}
		}
		mockHandler.setExpectTimeout(10000);
		long result2 = timer2.stop();
		CalledMethod[] methods = mockHandler.expect(methodNames);
		long result = timer.stop();
		
		//pick a method and verify right data came back for performance test
		//to make sure performance test is valid....
		ByteBuffer actualBuf = (ByteBuffer)methods[0].getAllParams()[1];
		String actual = HELPER.readString(actualBuf, actualBuf.remaining());
		assertEquals(payload, actual);
		log.info("payload="+actual);
		
		long readWriteTime = result/size;
		log.info("total write time         ="+result2);		
		log.info("total write/read time    ="+result);
		log.info("--time per write/read    ="+readWriteTime);		
	}	
}
