package org.playorm.nio.test.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockDataHandler;
import org.playorm.nio.api.testutil.MockNIOServer;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public abstract class ZNioSuperclassTest extends TestCase {

	private static final Logger log = Logger.getLogger(ZNioSuperclassTest.class.getName());
	
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
	protected abstract Settings getServerFactoryHolder();
	protected abstract Settings getClientFactoryHolder();	
	protected abstract String getChannelImplName();
	protected abstract String getServerChannelImplName();
	
	public ZNioSuperclassTest() {
		if(bufFactory == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FactoryCreator.KEY_IS_DIRECT, false);
			FactoryCreator creator = FactoryCreator.createFactory(null);
			bufFactory = creator.createBufferFactory(map);			
		}
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
		
		loopBack = InetAddress.getByName("127.0.0.1");	
		loopBackAnyPort = new InetSocketAddress(loopBack, 0);
		
		mockHandler = MockObjectFactory.createMock(DataListener.class);
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		mockConnect = MockObjectFactory.createMock(ConnectionCallback.class);
		client1 = chanMgr.createTCPChannel("ClientChannel", getClientFactoryHolder());		
	}
	
	protected void tearDown() throws Exception {
		log.info("CHAN MGR STOP");
		chanMgr.stop();
		chanMgr = null;
		log.info("MOCK SERVER STOP");
		mockServer.stop();
		log.info("check for warns");
		HandlerForTests.checkForWarnings();
		log.info("done");
	}
	
	//TODO: write test that proves if data comes between calls to Selector.select,
	//the next selector.select will still trigger even if we cleared the key from
	//the list of selected keys.
	public void testDataComesBetweenSelects() throws Exception {
		
	}
	
//	public void testClientThrowsIntoConnectCallback() throws Exception {
//		//make sure we are testing the right one....
//		Class c = Class.forName(getChannelImplName());
//		assertEquals("should be instance of correct channel type", c, client1.getClass());
//	
//		String msg = "some exception message";
//		IOException e = new IOException(msg);
//		mockConnect.addThrowException("connected", e);
//		
//		client1.bind(loopBackAnyPort);		
//		client1.registerForReads((DataHandler)mockHandler);
//		client1.connect(svrAddr, (ConnectCallback)mockConnect);
//
//		mockConnect.expect("connected");
//		TCPChannel svrChan = (TCPChannel)mockServer.expect(MockNIOServer.CONNECTED).getAllParams()[0];
//		assertEquals("should be instance of correct channel type", c, svrChan.getClass());
//
//		verifyDataPassing(svrChan);
//		verifyTearDown();		
//	}
	
	/**
	 * This cannot pass on linux right now as warnings end up in the log 
	 * from reads firing even though there should be none if not connected.
	 * We can fix this later if it is needed.
	 * 
	 * Order between TCPChannel.connect and TCPChannel.registerForRead
	 * results in different code paths...this is one of the tests.
	 * @throws Exception
	 */
	public void xtestRegisterForReadsBeforeConnect() throws Exception {
		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of correct channel type", c, client1.getClass());
		
		client1.bind(loopBackAnyPort);		
		client1.registerForReads((DataListener)mockHandler);
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		mockConnect.expect("connected");
		
		boolean isConnected = client1.isConnected();
		assertTrue("Client should be connected", isConnected);
		
		TCPChannel svrChan = ZNioFailureSuperclass.expectServerChannel(mockServer, c);

		verifyDataPassing(svrChan);
		verifyTearDown();		
	}
	
    /**
     * 
     */
    public void testConnectClose() throws Exception {
        //make sure we are testing the right one....
//        Class c = Class.forName(getChannelImplName());
//        assertEquals("should be instance of secure channel", c, client1.getClass());
    
        //no bind, just do connect to test port is not zero
        client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
        mockConnect.expect("connected");
        
        mockServer.expect(MockNIOServer.CONNECTED);

        verifyTearDown();        
    }

	/**
	 * Order between TCPChannel.connect and TCPChannel.registerForRead
	 * results in different code paths...this is one of the tests.
	 * @throws Exception
	 */	
	public void testRegisterForReadsAfterConnect() throws Exception {
		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of secure channel", c, client1.getClass());
	
		//no bind, just do connect to test port is not zero
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		mockConnect.expect("connected");
		log.info("connected");
		
		boolean isConnected = client1.isConnected();
		assertTrue("Client should be connected", isConnected);
		InetSocketAddress localAddr = client1.getLocalAddress();
		assertTrue("Port should not be 0", localAddr.getPort() != 0);
		

		TCPChannel svrChan = ZNioFailureSuperclass.expectServerChannel(mockServer, c);

		client1.registerForReads((DataListener)mockHandler);

		log.info("data passing");
		verifyDataPassing(svrChan);
		log.info("teardown");
		verifyTearDown();
		log.info("done");
	}
	
	/**
	 * Test closing socket before ChannelManager shutdown works.
	 * @throws Exception
	 */
	public void testCloseSvrSocketBeforeChannelMgrShutdown() throws Exception {
		Class c = Class.forName(getChannelImplName());
		client1.bind(loopBackAnyPort);
		client1.oldConnect(svrAddr);

		boolean isConnected = client1.isConnected();
		assertTrue("Client should be connected", isConnected);
		
		TCPChannel svrChan = ZNioFailureSuperclass.expectServerChannel(mockServer, c);
		client1.registerForReads((DataListener)mockHandler);
		
		verifyDataPassing(svrChan);
		
		svrChan.oldClose();	
		
		//shutdown channel manager first
		mockServer.stop();
		
		mockHandler.expect(MockNIOServer.FAR_END_CLOSED);
	}
	
	/**
	 * There was a bug where calling ChannelManager.shutdown before closing any sockets
	 * registered with that ChannelManager cannot be closed...well, at least this test
	 * proves when we close the test, the other side should receive that -1 indicating
	 * the far end closed the socket.
	 * 
	 * @throws Exception
	 */
	public void testCloseSocketAfterChannelMgrShutdown() throws Exception {
		Class c = Class.forName(getChannelImplName());
		
		client1.bind(loopBackAnyPort);
		client1.oldConnect(svrAddr);
		TCPChannel svrChan = ZNioFailureSuperclass.expectServerChannel(mockServer, c);
		
		client1.registerForReads((DataListener)mockHandler);

		verifyDataPassing(svrChan);

		//shutdown channel manager first....should all sockets be closed?  Right now
		//someone has to manually close all accepted sockets...ie. client responsibility.
		svrChan.oldClose();
		
		mockServer.stop();		
	
		//notice the Channelmanager on the client side has not shut down so we should
		//see a close event....
		mockHandler.expect(MockNIOServer.FAR_END_CLOSED);
	}
	
	public void testUnregisterReregisterForReads() throws Exception {
		Class c = Class.forName(getChannelImplName());
		
		client1.bind(loopBackAnyPort);
		client1.oldConnect(svrAddr);

		TCPChannel svrChan = ZNioFailureSuperclass.expectServerChannel(mockServer, c);
		client1.registerForReads((DataListener)mockHandler);

		ByteBuffer b = verifyDataPassing(svrChan);

		
		client1.unregisterForReads();
		b.rewind();
		svrChan.oldWrite(b);
		Thread.sleep(5000);
		mockHandler.expect(MockObject.NONE);
		
		client1.registerForReads((DataListener)mockHandler);
		CalledMethod m = mockHandler.expect(MockNIOServer.INCOMING_DATA);
		ByteBuffer actualBuf = (ByteBuffer)m.getAllParams()[1];
		String result = helper.readString(actualBuf, actualBuf.remaining());
		assertEquals("de", result);		
		
		verifyTearDown();
	}
	
	/**
	 * the first 1.5.0 jdk threw Errors on this test case
	 * instead of BindException or SocketException.  Our ChannelManager
	 * converts the Error back if it is a SocketException.
	 * @throws Exception
	 */
	public void testAlreadyBound150Jdk() throws Exception {
		client1.bind(loopBackAnyPort);
		try {
			client1.bind(loopBackAnyPort);
			fail("Should have thrown SocketException");
		} catch(NioException e) {}
	}
	
	/**
	 * This fixes the bug in the jdk where SocketChannel.getLocalPort
	 * return 0 instead of the port that was bound.
	 * 
	 * @throws Exception
	 */
	public void testTwoBindsOnPortZero() throws Exception {
		TCPChannel chan1 = chanMgr.createTCPChannel("chan1", getClientFactoryHolder());
		TCPChannel chan2 = chanMgr.createTCPChannel("chan2", getClientFactoryHolder());
		
		InetSocketAddress addr = new InetSocketAddress(loopBack, 0);
		chan1.bind(addr);
		chan2.bind(addr);
		
		int port1 = chan1.getLocalAddress().getPort();
		int port2 = chan2.getLocalAddress().getPort();

		assertTrue("port1 is zero, this is bad", port1 != 0);
		assertTrue("port2 is zero, this is bad", port2 != 0);
		assertTrue("port1==port2, this is bad port1="+port1, port1!=port2);
	}
	
	public void testBindThroughConnect() throws Exception {
		client1.oldConnect(svrAddr);		
		int port = client1.getLocalAddress().getPort();
		assertTrue("port is zero, this is bad", port != 0);
		mockServer.expect("connected");
		//verifyTearDown();
	}
	
	private ByteBuffer verifyDataPassing(TCPChannel svrChan) throws Exception {
		ByteBuffer b = ByteBuffer.allocate(10);
		helper.putString(b, "de");
		helper.doneFillingBuffer(b);
		int expectedWrote = b.remaining();
		log.fine("***********************************************");
		int actualWrite = client1.oldWrite(b);
		assertEquals(expectedWrote, actualWrite);
		
		CalledMethod m = mockServer.expect(MockNIOServer.INCOMING_DATA);
		TCPChannel actualChannel = (TCPChannel)m.getAllParams()[0];
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be correct type of channel", c, actualChannel.getClass());
		
		ByteBuffer actualBuf = (ByteBuffer)m.getAllParams()[1];
		String result = helper.readString(actualBuf, actualBuf.remaining());
		assertEquals("de", result);
		
		b.rewind();
		svrChan.oldWrite(b);
		
		m = mockHandler.expect(MockDataHandler.INCOMING_DATA);
		actualBuf = (ByteBuffer)m.getAllParams()[1];
		result = helper.readString(actualBuf, actualBuf.remaining());
		assertEquals("de", result);	
		return b;
	}
	
	private void verifyTearDown() throws IOException {
        log.info("local="+client1.getLocalAddress()+" remote="+client1.getRemoteAddress());
		log.info("CLIENT1 CLOSE");
		client1.oldClose();
		mockServer.expect(MockNIOServer.FAR_END_CLOSED);
	}
	
	protected Object getBufFactory() {
		return bufFactory;
	}

}
