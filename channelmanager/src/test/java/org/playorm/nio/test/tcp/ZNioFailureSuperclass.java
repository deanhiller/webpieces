package org.playorm.nio.test.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockDataHandler;
import org.playorm.nio.api.testutil.MockNIOServer;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.CloningBehavior;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.testcase.MockTestCase;

public abstract class ZNioFailureSuperclass extends MockTestCase {

	private static final Logger log = Logger.getLogger(ZNioFailureSuperclass.class.getName());
	
	private BufferFactory bufFactory;
	private InetSocketAddress svrAddr;
	private ChannelService chanMgr;
	private InetAddress loopBack;
	private InetSocketAddress loopBackAnyPort;
	private BufferHelper helper = ChannelServiceFactory.bufferHelper(null);

	private MockObject mockHandler;
	private MockObject mockConnect;
	private TCPChannel client1;
	private MockNIOServer mockServer;
	
	protected abstract ChannelService getClientChanMgr();
	protected abstract ChannelService getServerChanMgr();
	protected abstract Settings getClientFactoryHolder();
	protected abstract Settings getServerFactoryHolder();
	protected abstract String getChannelImplName();
	protected abstract String getServerChannelImplName();
	
	public ZNioFailureSuperclass(String name) {
		super(name);
		if(bufFactory == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FactoryCreator.KEY_IS_DIRECT, false);
			FactoryCreator creator = FactoryCreator.createFactory(null);
			bufFactory = creator.createBufferFactory(map);
		}
	}
	protected void setUpImpl() throws Exception {
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
		
		loopBack = InetAddress.getByName("127.0.0.1");	
		loopBackAnyPort = new InetSocketAddress(loopBack, 0);
		
		mockHandler = createMock(DataListener.class);
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		mockConnect = createMock(ConnectionCallback.class);
		client1 = chanMgr.createTCPChannel("ClientChannel", getClientFactoryHolder());		
	}
	
	protected void tearDownImpl() throws Exception {
		chanMgr.stop();
		chanMgr = null;
		mockServer.stop();
//		try {
//			HandlerForTests.checkForWarnings();
//			fail("There should be warnings in the log and there was none");
//		} catch(LogHasWarningException e) {			
//		}
	}
	
	public void testClientThrowsIntoConnectCallback() throws Exception {
		setNumberOfExpectedWarnings(1);
		
		log.info("hello");
		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of correct channel type", c, client1.getClass());
		log.info("class name"+client1.getClass().getName());
		String msg = "some exception message";
		IOException e = new IOException(msg);
		mockConnect.addThrowException("connected", e);
		
		client1.bind(loopBackAnyPort);		
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		client1.registerForReads((DataListener)mockHandler);
		
		mockConnect.expect("connected");
		TCPChannel svrChan = expectServerChannel(mockServer, c);

		verifyDataPassing(svrChan);
		verifyTearDown();		
	}
	
	static TCPChannel expectServerChannel(MockNIOServer mockServer, Class c) {
		CalledMethod method = mockServer.expect(MockNIOServer.CONNECTED);
		TCPChannel svrChan = (TCPChannel)method.getAllParams()[0];
		assertEquals("should be instance of correct channel type", c, svrChan.getClass());
		return svrChan;
	}

	public void testClientThrowsIntoDataHandlerIncomingData() throws Exception {
		setNumberOfExpectedWarnings(1);
		
		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of correct channel type", c, client1.getClass());
	
		mockHandler.addBehavior("incomingData", new ThrowAndClone());
		log.info("class name"+client1.getClass());
		client1.bind(loopBackAnyPort);		
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		client1.registerForReads((DataListener)mockHandler);
		
		mockConnect.expect("connected");
		TCPChannel svrChan = expectServerChannel(mockServer, c);

		verifyDataPassing(svrChan);
		verifyTearDown();		
	}
	
	private static class ThrowAndClone implements CloningBehavior {
		public void incomingData(Channel channel, DataChunk b) {	
			throw new RuntimeException("testing");
		}
		public Object[] incomingDataCloner(Channel channel, DataChunk chunk) {
			ByteBuffer b = chunk.getData();
			return new Object[] { channel, CloneByteBuffer.clone(b) };
		}		
		
	}
	
	public void testClientThrowsIntoDataHandlerFarEndClosed() throws Exception {
		setNumberOfExpectedWarnings(1);

		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of correct channel type", c, client1.getClass());
	
		String msg = "some exception message";
		IOException e = new IOException(msg);
		mockServer.addThrowException("farEndClosed", e);
		
		client1.bind(loopBackAnyPort);		
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		client1.registerForReads((DataListener)mockHandler);
		
		mockConnect.expect("connected");
		TCPChannel svrChan = expectServerChannel(mockServer, c);

		verifyDataPassing(svrChan);
		verifyTearDown();
		//shutdown of threaded CM is complicated...set stop method on threaded CM.
		//We must sleep or thread could be interrupted and never log an error.
		//This is a race condition, but only when we are stopping the ChannelManager
		specialCaseThreadedCM();
	}
	protected void specialCaseThreadedCM() throws InterruptedException {
		
	}
		
//	public void testClientThrowsIntoAcceptHandlerConnect() throws Exception {
//		setNumberOfExpectedWarnings(1);
//
//		//make sure we are testing the right one....
//		Class c = Class.forName(getChannelImplName());
//		assertEquals("should be instance of correct channel type", c, client1.getClass());
//	
//		String msg = "some exception message";
//		IOException e = new IOException(msg);
//		mockServer.addThrowException("connected", e);
//		
//		client1.bind(loopBackAnyPort);
//		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
//		client1.registerForReads((DataListener)mockHandler);
//		
//		mockConnect.expect("connected");
//		TCPChannel svrChan = expectServerChannel(mockServer, c);
//
//		verifyDataPassing(svrChan);
//		verifyTearDown();		
//	}
	
	private ByteBuffer verifyDataPassing(TCPChannel svrChan) throws Exception {
		ByteBuffer b = ByteBuffer.allocate(10);
		helper.putString(b, "de");
		helper.doneFillingBuffer(b);
		int expectedWrote = b.remaining();
		int actualWrite = client1.oldWrite(b);
		assertEquals(expectedWrote, actualWrite);
		
		CalledMethod m = mockServer.expect(MockNIOServer.INCOMING_DATA);
		TCPChannel actualChannel = (TCPChannel)m.getAllParams()[0];
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be correct type of channel", c, actualChannel.getClass());
		
		ByteBuffer actualBuf = (ByteBuffer)m.getAllParams()[1];
		String result = helper.readString(actualBuf, actualBuf.remaining());
		log.info("result len="+result.length());
		
		assertEquals("de", result);
		
		b.rewind();
		svrChan.oldWrite(b);
		
		m = mockHandler.expect(MockDataHandler.INCOMING_DATA);
		actualBuf = (ByteBuffer)m.getAllParams()[1];
		log.info("buffer remain="+actualBuf.remaining());
		result = helper.readString(actualBuf, actualBuf.remaining());
		log.info("---1st char="+(result.substring(0, 1).equals("de".substring(0, 1))));
		log.info("---2nd char="+(result.substring(1, 2).equals("de".substring(1, 2))));
		log.info("substring='"+result.substring(0,1)+"'");
		log.info("len="+"de".length()+"  2ndlen="+result.length());
		log.info("'de'"+" actual='"+result+"'"+"  result="+("de".equals(result)));
		assertEquals("de", result);	
		return b;
	}
	
	private void verifyTearDown() throws IOException {
		client1.oldClose();
		mockServer.expect(MockNIOServer.FAR_END_CLOSED);
	}
	
	protected Object getBufFactory() {
		return bufFactory;
	}
}
