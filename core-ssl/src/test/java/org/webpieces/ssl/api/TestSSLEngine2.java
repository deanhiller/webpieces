package org.webpieces.ssl.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.ConnectionState;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;

public class TestSSLEngine2 {

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
		clientEngine = AsyncSSLFactory.createParser("client", client, pool, clientListener);
		svrEngine = AsyncSSLFactory.createParser("svr", svr, pool, svrListener);
		
		Assert.assertEquals(ConnectionState.NOT_STARTED, clientEngine.getConnectionState());
		Assert.assertEquals(ConnectionState.NOT_STARTED, svrEngine.getConnectionState());
		
		clientEngine.beginHandshake();
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		ByteBuffer buffer = clientListener.getToSendToSocket().get(0);
		
		svrEngine.feedEncryptedPacket(buffer);
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		Runnable r = svrListener.getRunnable();
		r.run();
		
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		ByteBuffer buf = svrListener.getToSendToSocket().get(0);
		
		clientEngine.feedEncryptedPacket(buf);
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		clientListener.getRunnable().run();
	}
	
	@Test
	public void testBasic() throws GeneralSecurityException, IOException {
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		
		svrEngine.feedEncryptedPacket(buffers.get(0));
		svrListener.getRunnable().run();
		
		svrEngine.feedEncryptedPacket(buffers.get(1));
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		
		svrEngine.feedEncryptedPacket(buffers.get(2));
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);
		
		List<ByteBuffer> toClientBuffers = svrListener.getToSendToSocket();
		
		clientEngine.feedEncryptedPacket(toClientBuffers.get(0));
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		
		clientEngine.feedEncryptedPacket(toClientBuffers.get(1));
		Assert.assertEquals(ConnectionState.CONNECTED, clientEngine.getConnectionState());
		Assert.assertTrue(clientListener.connected);
		
		transferBigData();
	}
	
	private void transferBigData() {
		ByteBuffer b = ByteBuffer.allocate(17000);
		b.put((byte) 1);
		b.put((byte) 2);
		b.position(b.limit()-2); //simulate buffer full of 0's except first 2 and last 2
		b.put((byte) 3);
		b.put((byte) 4);
		b.flip();
		
		CompletableFuture<Void> future = clientEngine.feedPlainPacket(b);
		List<ByteBuffer> encrypted = clientListener.getToSendToSocket();
		//results in two ssl packets instead of the one that was fed in..
		Assert.assertEquals(2, encrypted.size());
		
		svrEngine.feedEncryptedPacket(encrypted.get(0));
		ByteBuffer buffer = svrListener.getToSendToClient().get(0);
		
		svrEngine.feedEncryptedPacket(encrypted.get(1));
		ByteBuffer buffer2 = svrListener.getToSendToClient().get(0);
		
		Assert.assertEquals(17000, buffer.remaining()+buffer2.remaining());
		
		Assert.assertFalse(future.isDone());
		
		List<CompletableFuture<Void>> futures = clientListener.getFutures();
		futures.get(0).complete(null);
		Assert.assertFalse(future.isDone());
		futures.get(1).complete(null);
		Assert.assertTrue(future.isDone());
	}

	@Test
	public void testCombineBuffers() {
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		ByteBuffer combine = combine(buffers);
		
		svrEngine.feedEncryptedPacket(combine);
		svrListener.getRunnable().run();
		
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);
		List<ByteBuffer> toClientBuffers = svrListener.getToSendToSocket();
		Assert.assertEquals(2, toClientBuffers.size());
	}
	
	private ByteBuffer combine(List<ByteBuffer> buffersToSend) {
		int size = 0;
		for(ByteBuffer b : buffersToSend) {
			size += b.remaining();
		}
		ByteBuffer buf = ByteBuffer.allocate(size);
		for(ByteBuffer b : buffersToSend) {
			buf.put(b);
		}
		
		buf.flip();
		return buf;
	}

	@Test
	public void testRunnableRunAfterNextPacket() {
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		
		svrEngine.feedEncryptedPacket(buffers.get(0));
		Runnable run = svrListener.getRunnable();
		
		svrEngine.feedEncryptedPacket(buffers.get(1));
		
		svrEngine.feedEncryptedPacket(buffers.get(2));
		
		run.run();
		
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);
		List<ByteBuffer> toClientBuffers = svrListener.getToSendToSocket();
		Assert.assertEquals(2, toClientBuffers.size());
	}
	
	@Test
	public void testHalfThenTooMuchFedInPacket() {
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		List<ByteBuffer> first = split(buffers.get(0));
		List<ByteBuffer> second = split(buffers.get(1));
		ByteBuffer halfAndHalf = combine(first.get(1), second.get(0));
		
		svrEngine.feedEncryptedPacket(first.get(0));
		
		svrEngine.feedEncryptedPacket(halfAndHalf);
		Runnable run = svrListener.getRunnable();
		run.run();
		
		svrEngine.feedEncryptedPacket(second.get(1));
		
		svrEngine.feedEncryptedPacket(buffers.get(2));
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);
	}

	@Test
	public void testHalfThenTooMuchFedInPacketAndRunnableDelayed() {
		List<ByteBuffer> buffers = clientListener.getToSendToSocket();
		List<ByteBuffer> first = split(buffers.get(0));
		List<ByteBuffer> second = split(buffers.get(1));
		ByteBuffer halfAndHalf = combine(first.get(1), second.get(0));
		
		svrEngine.feedEncryptedPacket(first.get(0));
		
		svrEngine.feedEncryptedPacket(halfAndHalf);
		Runnable run = svrListener.getRunnable();
		
		svrEngine.feedEncryptedPacket(second.get(1));
		
		run.run();
		
		svrEngine.feedEncryptedPacket(buffers.get(2));
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());		
		Assert.assertTrue(svrListener.connected);
	}
	
	private List<ByteBuffer> split(ByteBuffer byteBuffer) {
		int splitPoint = byteBuffer.remaining() / 2;
		byte[] one = new byte[splitPoint];
		byte[] two = new byte[byteBuffer.remaining() - splitPoint];
		byteBuffer.get(one);
		byteBuffer.get(two);
		if(byteBuffer.hasRemaining())
			throw new RuntimeException("bug, shoudl have consumed it all");		

		ByteBuffer buf1 = ByteBuffer.wrap(one);
		ByteBuffer buf2 = ByteBuffer.wrap(two);
		
		List<ByteBuffer> list = new ArrayList<>();
		list.add(buf1);
		list.add(buf2);
		
		return list;
	}
	
	private ByteBuffer combine(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) {
		ByteBuffer newBuf = ByteBuffer.allocate(byteBuffer.remaining()+byteBuffer2.remaining());
		newBuf.put(byteBuffer);
		newBuf.put(byteBuffer2);
		newBuf.flip();
		return newBuf;
	}

}
