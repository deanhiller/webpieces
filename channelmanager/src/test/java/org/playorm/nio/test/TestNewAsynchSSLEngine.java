package org.playorm.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.AsyncSSLEngineException;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.libs.SSLListener;
import org.playorm.nio.api.testutil.HandlerForTests;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;

import biz.xsoftware.mock.MockObject;

/**
 * Normally I would not separate out one class for testing, but when this
 * is integrated with the ChanMgr, testing becomes non-deterministic with
 * packets being separated and such.  This allows more deterministic
 * testing to fully test AsynchSSLEngine.
 * 
 * @author dean.hiller
 */
public class TestNewAsynchSSLEngine extends TestCase {

	private static final Logger log = Logger.getLogger(TestNewAsynchSSLEngine.class.getName());
	
	private BufferHelper helper = ChannelServiceFactory.bufferHelper(null);
	private MockSslListener serverList = new MockSslListener();
	private MockSslListener clientList = new MockSslListener();
	private AsyncSSLEngine serverEngine;
	private AsyncSSLEngine clientEngine;
	
	@Override
	protected void setUp() throws Exception {
		HandlerForTests.setupLogging();

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
		HandlerForTests.checkForWarnings();
	}
		
	public void testBasic() throws Exception {
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		ByteBuffer b = clientList.getPacketEncrypted();
		
		serverEngine.feedEncryptedPacket(b, null);
		Runnable r = serverList.getRunnable();

		r.run();
		b = serverList.getPacketEncrypted();

		clientEngine.feedEncryptedPacket(b, null);
		r = clientList.getRunnable();

		r.run();
		ByteBuffer b0 = clientList.getPacketEncrypted();

		serverEngine.feedEncryptedPacket(b0, null);
		ByteBuffer b1 = clientList.getPacketEncrypted();
		r = serverList.getRunnable();

		r.run();
				
		serverEngine.feedEncryptedPacket(b1, null);
		ByteBuffer b2 = clientList.getPacketEncrypted();

		serverEngine.feedEncryptedPacket(b2, null);		
		Assert.assertTrue(serverList.isEncryptedLinkEstablished());
		
		b0 = serverList.getPacketEncrypted();
		clientEngine.feedEncryptedPacket(b0, null);
		b1 = serverList.getPacketEncrypted();
		clientEngine.feedEncryptedPacket(b1, null);

		Assert.assertTrue(clientList.isEncryptedLinkEstablished());

		feedData(clientEngine, clientList, serverEngine, serverList);
		feedData(serverEngine, serverList, clientEngine, clientList);
	}

	/**
	 * Test case where there is one and one half packets in the buffer, so the second
	 * one gets a underflow and the buffer was not being reset properly.  This test
	 * proved a bug while all other tests passed so don't delete this as it is not
	 * a duplicate test case.
	 * 
	 * Test one and half packets should have caught this same bug but did not for
	 * some reason.
	 * 
	 * @throws Exception
	 */
	public void testLargePackets() throws Exception {
		testBasic();
		
		ByteBuffer b = ByteBuffer.allocate(5000);
		String payload = "hello";
		for(int i = 0; i < 3000; i++) {
			payload+="i";
		}
		helper.putString(b, payload);
		helper.doneFillingBuffer(b);
		
		clientEngine.feedPlainPacket(b, null);
		
		ByteBuffer encData1 = clientList.getPacketEncrypted();
		log.info("data="+encData1);
		
		b.rewind();
		clientEngine.feedPlainPacket(b, null);
		ByteBuffer encData2 = clientList.getPacketEncrypted();
		log.info("data="+encData2);		
		
		b.rewind();
		clientEngine.feedPlainPacket(b, null);
		ByteBuffer encData3 = clientList.getPacketEncrypted();
		log.info("data="+encData3);		
		
		ByteBuffer all = ByteBuffer.allocate(10000);
		all.clear();
		all.put(encData1);
		all.put(encData2);
		all.put(encData3);
		
		all.position(0);
		all.limit(3736);
		serverEngine.feedEncryptedPacket(all, null);
		Assert.assertNotNull(serverList.getPacketUnencrypted());

		all.limit(8736);
		all.position(3736);
		serverEngine.feedEncryptedPacket(all, null);
		Assert.assertNotNull(serverList.getPacketUnencrypted());
	}
	
	public void testClientClose() throws Exception {
		testBasic();
		
		clientEngine.initiateClose();
		
		ByteBuffer tmp = ByteBuffer.allocate(10);
		helper.putString(tmp, "asdf");
		helper.doneFillingBuffer(tmp);
		try {
			clientEngine.feedPlainPacket(tmp, null);
			fail("should have thrown exception");
		} catch(IllegalStateException e) {}
		
		ByteBuffer b = clientList.getPacketEncrypted();
		
		serverEngine.feedEncryptedPacket(b, null);
		
		b = serverList.getPacketEncrypted();
		Assert.assertTrue(serverList.isClosed());
		
		clientEngine.feedEncryptedPacket(b, null);
		
		Assert.assertTrue(clientList.isClosed());
	}

	public void testServerClose() throws Exception {
		testBasic();
		
		serverEngine.initiateClose();
		
		ByteBuffer b = serverList.getPacketEncrypted();
		
		clientEngine.feedEncryptedPacket(b, null);
		
		b = clientList.getPacketEncrypted();
		Assert.assertTrue(clientList.isClosed());
		
		serverEngine.feedEncryptedPacket(b, null);

		Assert.assertTrue(serverList.isClosed());
	}
	
	static void closeWithExpects(AsyncSSLEngine engine, MockObject sslListener) throws IOException {
		engine.close();
		
		String[] methodNames = new String[2];
		methodNames[0] = "packetEncrypted";
		methodNames[1] = "closed";
		sslListener.expect(methodNames);
	}
	
	static void feedPacket(AsyncSSLEngine engine, ByteBuffer b) throws Exception {
		int bytesLeft = 0;
		if(b.remaining() < 15)
			bytesLeft = 2;
		else
		    bytesLeft = 12;
		int lim = b.limit();
		b.limit(bytesLeft);
		engine.feedEncryptedPacket(b, null);
		b.limit(lim);
		engine.feedEncryptedPacket(b, null);
	}
	
	/**
	 * Two tests in one.  Test Runnable not finished on rehandshake and
	 * feeding packets.  Then test rehandshake failure.
	 * 
	 * The end of this test is kind of weird.  I thought a close handshake could be
	 * processed in the middle of a handshake to close the engine, but it appears
	 * this does not work.
	 * 
	 * @throws Exception
	 */
	public void testRunnableNotFinishedAndHandshakeFailure() throws Exception {
		testBasic();
		feedData(clientEngine, clientList, serverEngine, serverList);				
		
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		String expected = "abcasdfaasdfsdfsfsfdsfs";
		ByteBuffer data = ByteBuffer.allocate(100);
		helper.putString(data, expected);
		helper.doneFillingBuffer(data);
		clientEngine.feedPlainPacket(data, null);
		
		ByteBuffer b = clientList.getPacketEncrypted();
		ByteBuffer encData = clientList.getPacketEncrypted();
		
		serverEngine.feedEncryptedPacket(b, null);
		Runnable r = serverList.getRunnable();

		try {
			serverEngine.feedEncryptedPacket(encData, null);
			fail("Should have thrown exception since Runnable has not completed yet");
		} catch(IllegalStateException e) {}
		
		try {
			data.rewind();
			serverEngine.feedPlainPacket(data, null);
			fail("Should have thrown exception since Runnable has not completed yet");			
		} catch(IllegalStateException e) {}
		
		r.run();
		
		b = serverList.getPacketEncrypted();
		
		try {
			clientEngine.feedEncryptedPacket(data, null);
			fail("Should have thrown exception");
		} catch(AsyncSSLEngineException e) {}
		
		b = clientList.getPacketEncrypted();
		Assert.assertTrue(clientList.isClosed());
		
		try {
			serverEngine.feedEncryptedPacket(b, null);
			fail("not sure why this throws exceptoin...I expected it to be able to take a close handshake message");
		} catch(AsyncSSLEngineException e) {}
		
		serverList.getPacketEncrypted();
		Assert.assertTrue(serverList.isClosed());
	}
	
	public void testDataDuringRehandshake() throws Exception {
		testBasic();
		feedData(clientEngine, clientList, serverEngine, serverList);				
		
		log.fine("B*******************************************");
		clientEngine.beginHandshake();
		String expected = "abc";
		ByteBuffer data = ByteBuffer.allocate(10);
		helper.putString(data, expected);
		helper.doneFillingBuffer(data);
		clientEngine.feedPlainPacket(data, null);
		
		ByteBuffer b = clientList.getPacketEncrypted();
		ByteBuffer encData = clientList.getPacketEncrypted();
		
		serverEngine.feedEncryptedPacket(b, null);
		Runnable r = serverList.getRunnable();
		r.run();
		ByteBuffer hsMsg = serverList.getPacketEncrypted();	
		
		serverEngine.feedEncryptedPacket(encData, null);
		
		b = serverList.getAssembledUnencryptedPacket();
		String actual = helper.readString(b, b.remaining());
		assertEquals(expected, actual);	
		
		clientEngine.feedEncryptedPacket(hsMsg, null);
		r = clientList.getRunnable();
		
		r.run();

//		feedData(clientEngine, clientList, serverEngine, serverList);

		ByteBuffer b0 = clientList.getPacketEncrypted();		
//		feedData(clientEngine, clientList, serverEngine, serverList);

		serverEngine.feedEncryptedPacket(b0, null);
		ByteBuffer b1 = clientList.getPacketEncrypted();
		r = serverList.getRunnable();

		r.run();
				
		serverEngine.feedEncryptedPacket(b1, null);
		ByteBuffer b2 = clientList.getPacketEncrypted();		

		serverEngine.feedEncryptedPacket(b2, null);		
		
		b0 = serverList.getPacketEncrypted();		
		clientEngine.feedEncryptedPacket(b0, null);
		b1 = serverList.getPacketEncrypted();
		clientEngine.feedEncryptedPacket(b1, null);

//		feedData(clientEngine, clientList, serverEngine, serverList);		
	}
	
	private void feedData(AsyncSSLEngine from, MockSslListener fromList, AsyncSSLEngine to, MockSslListener toList) throws Exception {
		String expected = "abc";
		ByteBuffer data = ByteBuffer.allocate(10);
		helper.putString(data, expected);
		helper.doneFillingBuffer(data);
		from.feedPlainPacket(data, null);

		ByteBuffer encData = fromList.getPacketEncrypted();
		
		to.feedEncryptedPacket(encData, null);

		ByteBuffer b = toList.getAssembledUnencryptedPacket();
		String actual = helper.readString(b, b.remaining());
		assertEquals(expected, actual);			
	}
	
	/**
	 * Once close is started, incoming data may still keep coming.  Instead of throwing
	 * exception after exception when getting this data, we should drop it on the floor
	 * as in many systems it is expected to keep getting data. 
	 * 
	 * @throws Exception
	 */
	public void testDataDuringClose() throws Exception {
		testBasic();
		clientEngine.initiateClose();
		
		ByteBuffer data = ByteBuffer.allocate(10);
		helper.putString(data, "asdf");
		helper.doneFillingBuffer(data);
		serverEngine.feedPlainPacket(data, null);
		ByteBuffer encData = serverList.getPacketEncrypted();
		
		String expected = "asdf";
		ByteBuffer tmp = ByteBuffer.allocate(10);
		helper.putString(tmp, expected);
		helper.doneFillingBuffer(tmp);
		try {
			clientEngine.feedPlainPacket(tmp, null);
			fail("should have thrown exception");
		} catch(IllegalStateException e) {}
		
		ByteBuffer b = clientList.getPacketEncrypted();
		
		serverEngine.feedEncryptedPacket(b, null);
		
		ByteBuffer hsMsg = serverList.getPacketEncrypted();
		Assert.assertTrue(serverList.isClosed());
		
		clientEngine.feedEncryptedPacket(encData, null); 
		b = clientList.getAssembledUnencryptedPacket();
		log.info("******buffer="+b);
		String actual = helper.readString(b, b.remaining());
		assertEquals(expected, actual);
		
		clientEngine.feedEncryptedPacket(hsMsg, null);

		Assert.assertTrue(clientList.isClosed());
	}

}
