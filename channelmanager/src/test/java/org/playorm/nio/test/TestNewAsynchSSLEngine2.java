package org.playorm.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import junit.framework.TestCase;

import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.libs.SSLListener;
import org.playorm.nio.api.testutil.CloneByteBuffer;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;

import biz.xsoftware.mock.CalledMethod;
import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

/**
 * Normally I would not separate out one class for testing, but when this
 * is integrated with the ChanMgr, testing becomes non-deterministic with
 * packets being separated and such.  This allows more deterministic
 * testing to fully test AsynchSSLEngine.
 * 
 * @author dean.hiller
 */
public class TestNewAsynchSSLEngine2 extends TestCase {

	private static final Logger log = Logger.getLogger(TestNewAsynchSSLEngine2.class.getName());
	
	private BufferHelper helper = ChannelServiceFactory.bufferHelper(null);
	private MockObject serverList = MockObjectFactory.createMock(SSLListener.class);
	private MockObject clientList = MockObjectFactory.createMock(SSLListener.class);
	private AsyncSSLEngine serverEngine;
	private AsyncSSLEngine clientEngine;
	
	@Override
	protected void setUp() throws Exception {
		HandlerForTests.setupLogging();

		serverList.setDefaultBehavior("packetEncrypted", new CloneByteBuffer());
		clientList.setDefaultBehavior("packetEncrypted", new CloneByteBuffer());
		SSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		FactoryCreator creator = FactoryCreator.createFactory(null);
		
		SSLEngine wrappedSvr = sslEngineFactory.createEngineForServerSocket();				
		serverEngine = creator.createSSLEngine("[serverAsynch] ", wrappedSvr, null);
		serverEngine.setListener((SSLListener)serverList);
		
		SSLEngine wrappedClient = sslEngineFactory.createEngineForSocket();			
		clientEngine = creator.createSSLEngine("[clientEngine] ", wrappedClient, null);
		clientEngine.setListener((SSLListener)clientList);
	}
	
	@Override
	protected void tearDown() throws Exception {		
		if(!clientEngine.isClosed())
			closeWithExpects(clientEngine, clientList);
		if(!serverEngine.isClosed())
			closeWithExpects(serverEngine, serverList);
		
		HandlerForTests.checkForWarnings();
		clientList.expect(MockObject.NONE);
		serverList.expect(MockObject.NONE);
	}
		
	private void closeWithExpects(AsyncSSLEngine engine, MockObject sslListener) throws IOException {
		TestNewAsynchSSLEngine.closeWithExpects(engine, sslListener);
//		engine.close();
//		
//		String[] methodNames = new String[2];
//		methodNames[0] = "packetEncrypted";
//		methodNames[1] = "closed";
//		sslListener.expect(methodNames);
	}
	
	/**
	 * This tests the Runnable task being run in between packets such that it
	 * should not cause packet feeds to create new Runnables, and can
	 * run before all packets are in.
	 */
	public void testDelayedRunTask() throws Exception {
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		CalledMethod m = clientList.expect("packetEncrypted");
		ByteBuffer b = (ByteBuffer)m.getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b, null);
		m = serverList.expect("runTask");
		Runnable r = (Runnable)m.getAllParams()[0];

		r.run();
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];

		clientEngine.feedEncryptedPacket(b, null);
		m = clientList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];

		r.run();
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];		

		serverEngine.feedEncryptedPacket(b0, null);
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		m = serverList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b1, null);
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];

		//THIS IS THE DELAYED RUN TASK run after second feed of data to sslEngine...
		r.run();
			
		serverEngine.feedEncryptedPacket(b2, null);		
		String[] methodNames1 = new String[3];
		methodNames1[0] = "packetEncrypted";
		methodNames1[1] = "packetEncrypted";
		methodNames1[2] = "encryptedLinkEstablished";
		CalledMethod[] methods1 = serverList.expect(methodNames1);
		
		ByteBuffer b01 = (ByteBuffer)methods1[0].getAllParams()[0];	
		clientEngine.feedEncryptedPacket(b01, null);
		ByteBuffer b11 = (ByteBuffer)methods1[1].getAllParams()[0];
		clientEngine.feedEncryptedPacket(b11, null);
		
		clientList.expect("encryptedLinkEstablished");
		log.fine("E*******************************************");
	}
	
	/**
	 * This tests the situation where the Runnable must tell the engine
	 * to reprocess the buffer itself.
	 */
	public void testReallyDelayedRunTask() throws Exception {
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		CalledMethod m = clientList.expect("packetEncrypted");
		ByteBuffer b = (ByteBuffer)m.getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b, null);
		m = serverList.expect("runTask");
		Runnable r = (Runnable)m.getAllParams()[0];

		r.run();
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];

		clientEngine.feedEncryptedPacket(b, null);
		m = clientList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];

		r.run();
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];		

		serverEngine.feedEncryptedPacket(b0, null);
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		m = serverList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b1, null);
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];		

		serverEngine.feedEncryptedPacket(b2, null);		
		String[] methodNames1 = new String[3];
		
		//THIS IS THE REALLY DELAYED RUN TASK run after all 3 packets are fed
		//to ssl engine
		r.run();
		
		methodNames1[0] = "packetEncrypted";
		methodNames1[1] = "packetEncrypted";
		methodNames1[2] = "encryptedLinkEstablished";
		CalledMethod[] methods1 = serverList.expect(methodNames1);
		
		ByteBuffer b01 = (ByteBuffer)methods1[0].getAllParams()[0];		
		clientEngine.feedEncryptedPacket(b01, null);
		ByteBuffer b11 = (ByteBuffer)methods1[1].getAllParams()[0];
		clientEngine.feedEncryptedPacket(b11, null);
		
		clientList.expect("encryptedLinkEstablished");
		log.fine("E*******************************************");
	}
	
	public void testHalfPackets() throws Exception {
		clientEngine.beginHandshake();
		
		CalledMethod m = clientList.expect("packetEncrypted");
		ByteBuffer b = (ByteBuffer)m.getAllParams()[0];

		feedPacket(serverEngine, b);
		
		m = serverList.expect("runTask");		
		Runnable r = (Runnable)m.getAllParams()[0];
		r.run();
		
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];
		log.fine("remain1="+b.remaining());
		
		feedPacket(clientEngine, b);
		
		m = clientList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];
		feedPacket(serverEngine, b0);
		
		m = serverList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		feedPacket(serverEngine, b1);
		
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];
		feedPacket(serverEngine, b2);		
		
		methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "encryptedLinkEstablished";
		methods = serverList.expect(methodNames);
		
		b0 = (ByteBuffer)methods[0].getAllParams()[0];
		feedPacket(clientEngine, b0);
		
		b1 = (ByteBuffer)methods[1].getAllParams()[0];
		feedPacket(clientEngine, b1);
		
		clientList.expect("encryptedLinkEstablished");		
	}
	
	public void testCombinedPackets() throws Exception {
		clientEngine.beginHandshake();
		
		CalledMethod m;
		ByteBuffer b;
		
		CalledMethod m1 = clientList.expect("packetEncrypted");
		ByteBuffer b3 = (ByteBuffer)m1.getAllParams()[0];
		serverEngine.feedEncryptedPacket(b3, null);
		
		m = serverList.expect("runTask");		
		Runnable r = (Runnable)m.getAllParams()[0];
		r.run();
		
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];
		clientEngine.feedEncryptedPacket(b, null);
		
		m = clientList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];
		
		int total = b0.remaining()+b1.remaining()+b2.remaining();
		ByteBuffer combined = ByteBuffer.allocate(total);
		combined.put(b0);
		combined.put(b1);
		combined.put(b2);
		helper.doneFillingBuffer(combined);
		serverEngine.feedEncryptedPacket(combined, null);		

		m = serverList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "encryptedLinkEstablished";
		methods = serverList.expect(methodNames);

		b0 = (ByteBuffer)methods[0].getAllParams()[0];
		b1 = (ByteBuffer)methods[1].getAllParams()[0];
		total = b0.remaining()+b1.remaining();
		combined = ByteBuffer.allocate(total);
		combined.put(b0);
		combined.put(b1);		
		helper.doneFillingBuffer(combined);

		clientEngine.feedEncryptedPacket(combined, null);
		
		clientList.expect("encryptedLinkEstablished");		
	}
	
	public void testOneAndHalfPackets() throws Exception {
		clientEngine.beginHandshake();
		
		CalledMethod m = clientList.expect("packetEncrypted");
		ByteBuffer b = (ByteBuffer)m.getAllParams()[0];
		serverEngine.feedEncryptedPacket(b, null);
		
		m = serverList.expect("runTask");		
		Runnable r = (Runnable)m.getAllParams()[0];
		r.run();
		
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];
		clientEngine.feedEncryptedPacket(b, null);
		
		m = clientList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];
				
		int total = b0.remaining()+b1.remaining()+b2.remaining();
		ByteBuffer combined = ByteBuffer.allocate(total);
		combined.put(b0);
		
		int lim = b1.limit();
		b1.limit(3); //we only want to put part of b1 in payload
		combined.put(b1);
		helper.doneFillingBuffer(combined);
		serverEngine.feedEncryptedPacket(combined, null);
		
		combined.clear();
		b1.limit(lim);
		combined.put(b1);
		combined.put(b2);
		
		helper.doneFillingBuffer(combined);
		serverEngine.feedEncryptedPacket(combined, null);		

		m = serverList.expect("runTask");		
		r = (Runnable)m.getAllParams()[0];
		r.run();
		
		methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "encryptedLinkEstablished";
		methods = serverList.expect(methodNames);

		b0 = (ByteBuffer)methods[0].getAllParams()[0];
		b1 = (ByteBuffer)methods[1].getAllParams()[0];
		total = b0.remaining()+b1.remaining();
		combined = ByteBuffer.allocate(total);
		combined.put(b0);
		combined.put(b1);		
		helper.doneFillingBuffer(combined);

		clientEngine.feedEncryptedPacket(combined, null);
		
		clientList.expect("encryptedLinkEstablished");		
	}
	
	public void testRunInMiddleOfPacket() throws Exception {
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		CalledMethod m = clientList.expect("packetEncrypted");
		ByteBuffer b = (ByteBuffer)m.getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b, null);
		m = serverList.expect("runTask");
		Runnable r = (Runnable)m.getAllParams()[0];

		r.run();
		m = serverList.expect("packetEncrypted");
		b = (ByteBuffer)m.getAllParams()[0];

		clientEngine.feedEncryptedPacket(b, null);
		m = clientList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];

		r.run();
		String[] methodNames = new String[3];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "packetEncrypted";
		methodNames[2] = "packetEncrypted";
		CalledMethod[] methods = clientList.expect(methodNames);
		
		ByteBuffer b0 = (ByteBuffer)methods[0].getAllParams()[0];
		ByteBuffer b1 = (ByteBuffer)methods[1].getAllParams()[0];
		ByteBuffer b2 = (ByteBuffer)methods[2].getAllParams()[0];
		
		serverEngine.feedEncryptedPacket(b0, null);
		m = serverList.expect("runTask");
		r = (Runnable)m.getAllParams()[0];
	
		int total = b1.remaining()+b2.remaining();
		ByteBuffer combined = ByteBuffer.allocate(total);
		
		int lim = b1.limit();
		b1.limit(3); //we only want to put part of b1 in payload
		combined.put(b1);
		helper.doneFillingBuffer(combined);
		serverEngine.feedEncryptedPacket(combined, null);
		
		//run the task after some of the previous packet fed, then feed rest of packet
		r.run();
		
		combined.clear();
		b1.limit(lim);
		combined.put(b1);
		combined.put(b2);
		
		helper.doneFillingBuffer(combined);
		serverEngine.feedEncryptedPacket(combined, null);
		
		String[] methodNames1 = new String[3];
		methodNames1[0] = "packetEncrypted";
		methodNames1[1] = "packetEncrypted";
		methodNames1[2] = "encryptedLinkEstablished";
		CalledMethod[] methods1 = serverList.expect(methodNames1);
		
		b0 = (ByteBuffer)methods1[0].getAllParams()[0];		
		clientEngine.feedEncryptedPacket(b0, null);
		b1 = (ByteBuffer)methods1[1].getAllParams()[0];
		clientEngine.feedEncryptedPacket(b1, null);
		
		clientList.expect("encryptedLinkEstablished");		
	}

	private void feedPacket(AsyncSSLEngine engine, ByteBuffer b) throws Exception {
		TestNewAsynchSSLEngine.feedPacket(engine, b);
	}

}
