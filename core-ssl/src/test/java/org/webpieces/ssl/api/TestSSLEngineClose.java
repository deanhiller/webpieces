package org.webpieces.ssl.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.net.ssl.SSLEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.ConnectionState;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;

public class TestSSLEngineClose {

	private AsyncSSLEngine clientEngine;
	private AsyncSSLEngine svrEngine;
	private MockSslListener clientListener = new MockSslListener();
	private MockSslListener svrListener = new MockSslListener();

	@Before
	public void setup() throws GeneralSecurityException, IOException {
		MockSSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		SSLEngine client = sslEngineFactory.createEngineForSocket();
		SSLEngine svr = sslEngineFactory.createEngineForServerSocket();
		clientEngine = AsyncSSLFactory.create("client", client, pool, clientListener);
		svrEngine = AsyncSSLFactory.create("svr", svr, pool, svrListener);
		
		clientEngine.beginHandshake();
		ByteBuffer buffer = clientListener.getToSendToSocket().get(0);

		svrEngine.feedEncryptedPacket(buffer);

		svrListener.getRunnable().run();
		
		ByteBuffer buf = svrListener.getToSendToSocket().get(0);
		clientEngine.feedEncryptedPacket(buf);
		clientListener.getRunnable().run();
		
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		
		svrEngine.feedEncryptedPacket(buffers.get(0));
		svrListener.getRunnable().run();
		
		svrEngine.feedEncryptedPacket(buffers.get(1));
		
		svrEngine.feedEncryptedPacket(buffers.get(2));
		Assert.assertTrue(svrListener.connected);
		
		List<ByteBuffer> toClientBuffers = svrListener.getToSendToSocket();
		
		clientEngine.feedEncryptedPacket(toClientBuffers.get(0));
		clientEngine.feedEncryptedPacket(toClientBuffers.get(1));

		Assert.assertTrue(clientListener.connected);
		
		transferBigData();
	}
	
	@Test
	public void testBasicCloseFromServer() throws GeneralSecurityException, IOException {
		svrEngine.close();
		List<ByteBuffer> bufs = svrListener.getToSendToSocket();
		
		clientEngine.feedEncryptedPacket(bufs.get(0));
		Assert.assertTrue(clientListener.closed);
		Assert.assertFalse(clientListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, clientEngine.getConnectionState());
		ByteBuffer buf = clientListener.getToSendToSocket().get(0);
		
		svrEngine.feedEncryptedPacket(buf);
		Assert.assertTrue(svrListener.closed);
		Assert.assertTrue(svrListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, svrEngine.getConnectionState());
	}
	
	@Test
	public void testBasicCloseFromClient() throws GeneralSecurityException, IOException {
		clientEngine.close();
		Assert.assertFalse(clientListener.closed);
		Assert.assertEquals(ConnectionState.DISCONNECTING, clientEngine.getConnectionState());
		ByteBuffer bufs = clientListener.getToSendToSocket().get(0);
		
		svrEngine.feedEncryptedPacket(bufs);
		Assert.assertTrue(svrListener.closed);
		Assert.assertFalse(svrListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, svrEngine.getConnectionState());
		ByteBuffer buf = svrListener.getToSendToSocket().get(0);
		
		clientEngine.feedEncryptedPacket(buf);
		Assert.assertTrue(clientListener.closed);
		Assert.assertTrue(clientListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, clientEngine.getConnectionState());
		
	}
	
	@Test
	public void testBothEndsAtSameTime() {
		clientEngine.close();
		svrEngine.close();
		
		ByteBuffer clientBuf = clientListener.getToSendToSocket().get(0);
		ByteBuffer svrBuf = svrListener.getToSendToSocket().get(0);
		
		clientEngine.feedEncryptedPacket(svrBuf);
		Assert.assertTrue(clientListener.closed);
		Assert.assertTrue(clientListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, clientEngine.getConnectionState());
		
		svrEngine.feedEncryptedPacket(clientBuf);
		Assert.assertTrue(svrListener.closed);
		Assert.assertTrue(svrListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, svrEngine.getConnectionState());
	}
	
	@Test
	public void testRaceWithCloseCall() {
		
		svrEngine.close();
		ByteBuffer svrBuf = svrListener.getToSendToSocket().get(0);
		
		clientEngine.feedEncryptedPacket(svrBuf);
		Assert.assertTrue(clientListener.closed);
		Assert.assertFalse(clientListener.clientInitiated);
		Assert.assertEquals(ConnectionState.DISCONNECTED, clientEngine.getConnectionState());
		ByteBuffer clientBuf = clientListener.getToSendToSocket().get(0);
		
		clientEngine.close();
		
		svrEngine.feedEncryptedPacket(clientBuf);
	}
	
	private void transferBigData() {
		ByteBuffer b = ByteBuffer.allocate(17000);
		b.put((byte) 1);
		b.put((byte) 2);
		b.position(b.limit()-2); //simulate buffer full of 0's except first 2 and last 2
		b.put((byte) 3);
		b.put((byte) 4);
		b.flip();
		
		clientEngine.feedPlainPacket(b);
		List<ByteBuffer> encrypted = clientListener.getToSendToSocket();
		//results in two ssl packets instead of the one that was fed in..
		Assert.assertEquals(2, encrypted.size());
		
		svrEngine.feedEncryptedPacket(encrypted.get(0));
		ByteBuffer buffer = svrListener.getToSendToClient().get(0);
		
		svrEngine.feedEncryptedPacket(encrypted.get(1));
		ByteBuffer buffer2 = svrListener.getToSendToClient().get(0);
		
		Assert.assertEquals(17000, buffer.remaining()+buffer2.remaining());
	}


}
