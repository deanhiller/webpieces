package org.playorm.nio.test.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import junit.framework.TestCase;

import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.libs.SSLListener;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockNIOServer;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;

import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public class TestMoreSecureChanMgr extends TestCase {

	private static final Logger log = Logger.getLogger(TestMoreSecureChanMgr.class.getName());
	
	private InetSocketAddress svrAddr;
	private ChannelService chanMgr;
	private InetAddress loopBack;
	private InetSocketAddress loopBackAnyPort;
//	private BufferHelper helper = ChannelManagerFactory.bufferHelper(null);

	private MockObject mockHandler = MockObjectFactory.createMock(DataListener.class);
	private MockObject mockConnect = MockObjectFactory.createMock(ConnectionCallback.class);
	private MockObject mockSSLListener = MockObjectFactory.createMock(SSLListener.class);
	private TCPChannel client1;
	private MockNIOServer mockServer;
	private AsyncSSLEngine sslEngine;

	private ChannelServiceFactory basicFactory;

	private BufferFactory bufFactory;

	private FactoryCreator creator;
	
	public TestMoreSecureChanMgr() {
		basicFactory = ChannelServiceFactory.createFactory(null);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		creator = FactoryCreator.createFactory(null);
		bufFactory = creator.createBufferFactory(map);
	}
	
	protected ChannelService getClientChanMgr() throws Exception {
		
		Map<String, Object> factoryName = new HashMap<String, Object>();
		factoryName.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_SECURE_CHANNEL_MGR);
		factoryName.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basicFactory);
		ChannelServiceFactory secureFactory = ChannelServiceFactory.createFactory(factoryName);
		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "client");		
		p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
		ChannelService chanMgr = secureFactory.createChannelManager(p);		
		return chanMgr;
	}

	protected ChannelService getServerChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "server");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
		ChannelService svcChanMgr = basicFactory.createChannelManager(p);
		
		return svcChanMgr;
	}
	
	protected String getChannelImplName() {
		return "biz.xsoftware.impl.nio.cm.secure.TCPChannelImpl";
	}	
	
	protected void setUp() throws Exception {
		SSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		Settings clientFactoryHolder = new Settings(sslEngineFactory, null);

		//use this engine to feed data back from server...
		SSLEngine wrappedSvr = sslEngineFactory.createEngineForServerSocket();		
		sslEngine = creator.createSSLEngine("[serverAsynch] ", wrappedSvr, null);
		sslEngine.setListener((SSLListener)mockSSLListener);
		
		//here I keep using the same channel manager on purpose, just
		//so we get testing between tests that the channel manager shutdown
		//and started back up cleanly.....		
		if(chanMgr == null) {
			chanMgr = getClientChanMgr();
		}
		if(mockServer == null) {
			ChannelService svcChanMgr = getServerChanMgr();
			mockServer = new MockNIOServer(svcChanMgr, null);
		}
		chanMgr.start();		
		svrAddr = mockServer.start();
		log.fine("server port ="+svrAddr);
		
		loopBack = InetAddress.getByName("127.0.0.1");
		loopBackAnyPort = new InetSocketAddress(loopBack, 0);
		
		mockHandler.setDefaultBehavior("incomingData", new CloneByteBuffer());
		mockSSLListener.setDefaultBehavior("packetEncrypted", new CloneByteBuffer());
		client1 = chanMgr.createTCPChannel("ClientChannel", clientFactoryHolder);
	}
	
	protected void tearDown() throws Exception {
		chanMgr.stop();
		chanMgr = null;
		mockServer.stop();
	}
	/**
	 * On windows, happened to run across a test while writing another test where chanMgr
	 * would throw a CancelledKeyException.  This test reproduced and help fix
	 * that problem.  It only reproduced it 70% of the time, so it is race 
	 * condition specific.
	 * 
	 * Now, on linux ran into NotYetConnectedException because as soon as you register for
	 * reads jdk1.5.0_05, the selector keeps firing a key that is ready for reads which is 
	 * impossible since it is not connected.  Then it gets connected and the test only fails
	 * because there is a warning in the log, but otherwise everything keeps working but there
	 * is a performance penalty because of all the NotYetConnectedExceptions being thrown.  
	 * Calling registerForReads after calling connect fixes the problem though.
	 * 
	 * @throws Exception
	 */
	public void xtestNoCancelledKeyException() throws Exception {
		HandlerForTests.setupLogging();
		
		//make sure we are testing the right one....
		Class c = Class.forName(getChannelImplName());
		assertEquals("should be instance of correct channel type", client1.getClass(), c);
		
		client1.bind(loopBackAnyPort);		
		client1.oldConnect(svrAddr, (ConnectionCallback)mockConnect);
		client1.registerForReads((DataListener)mockHandler);
		
		String[] s = new String[2];
		s[0] = MockNIOServer.CONNECTED;
		s[1] = MockNIOServer.INCOMING_DATA;
		
		mockServer.expect(s);
		client1.registerForReads((DataListener)mockHandler);
	
		log.info("verify teardown");
		verifyTearDown();
		
		HandlerForTests.checkForWarnings();
	}
	
	/**
	 * Fail the handshake in a few different locations to see if server
	 * cleans up after itself....
	 *
	 */
	public void testHandshakeFailure() {
		
	}
	
	public void testRegisterForReadsDuringHandshake() {
		
	}

	/**
	 * Have client just close the socket and see how server reacts!!!!
	 *
	 */
	public void testBadClientOnClose() {
		
	}
	
//	private static TCPChannel expectServerChannel(MockNIOServer mockServer) {
//		String[] methodNames = new String[2];
//		methodNames[0] = MockNIOServer.ABOUT_TO_ACCEPT;
//		methodNames[1] = MockNIOServer.CONNECTED;
//		CalledMethod[] methods = mockServer.expect(methodNames);
//		TCPChannel svrChan = (TCPChannel)methods[1].getAllParams()[0];
//		return svrChan;
//	}
	
	private void verifyTearDown() throws IOException {
		client1.oldClose();
		String[] methodNames = new String[2];
		methodNames[0] = "incomingData"; //close handshake packet
		methodNames[1] = MockNIOServer.FAR_END_CLOSED;
		mockServer.expect(methodNames);		
	}	
}
