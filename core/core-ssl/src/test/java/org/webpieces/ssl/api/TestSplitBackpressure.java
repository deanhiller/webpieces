package org.webpieces.ssl.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.TwoPools;
import org.webpieces.data.api.BufferPool;
import org.webpieces.ssl.api.MockSslListener.BufferedFuture;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestSplitBackpressure {

	private AsyncSSLEngine clientEngine;
	private AsyncSSLEngine svrEngine;
	private MockSslListener clientListener = new MockSslListener();
	private MockSslListener svrListener = new MockSslListener();

	@Before
	public void setup() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		System.setProperty("jdk.tls.server.protocols", "TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
		
		SSLMetrics metrics = new SSLMetrics("", new SimpleMeterRegistry());
		MockSSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		BufferPool pool = new TwoPools("p1", new SimpleMeterRegistry());
		SSLEngine client = sslEngineFactory.createEngineForSocket();
		SSLEngine svr = sslEngineFactory.createEngineForServerSocket();
		clientEngine = AsyncSSLFactory.create("client", client, pool, clientListener, metrics);
		svrEngine = AsyncSSLFactory.create("svr", svr, pool, svrListener, metrics);
		
		Assert.assertEquals(ConnectionState.NOT_STARTED, clientEngine.getConnectionState());
		Assert.assertEquals(ConnectionState.NOT_STARTED, svrEngine.getConnectionState());
		
		CompletableFuture<Void> clientFuture = clientEngine.beginHandshake();
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		BufferedFuture toSend = clientListener.getSingleHandshake();
		
		assertFutureJustResolved(clientFuture, toSend);
		
		List<ByteBuffer> split = split(toSend.engineToSocketData);
		//TODO: feed in this packet as two and resolving 'both' resolves the future returned
		CompletableFuture<Void> svrFuture = svrEngine.feedEncryptedPacket(split.get(0));
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		
		CompletableFuture<Void> svrFuture2 = svrEngine.feedEncryptedPacket(split.get(1));
		
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		BufferedFuture toSend2 = svrListener.getSingleHandshake();

		Assert.assertFalse(svrFuture.isDone());
		Assert.assertFalse(svrFuture2.isDone());
		toSend2.future.complete(null);
		svrFuture.get(2, TimeUnit.SECONDS);
		svrFuture2.get(2, TimeUnit.SECONDS);
		
		//TODO: feed 2 halves of this packet in and test resolution of future
		List<ByteBuffer> split2 = split(toSend2.engineToSocketData);		
		CompletableFuture<Void> clientFuture1 = clientEngine.feedEncryptedPacket(split2.get(0));
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());

		CompletableFuture<Void> clientFuture2 = clientEngine.feedEncryptedPacket(split2.get(1));

		List<BufferedFuture> buffers = clientListener.getHandshake();
		buffers.get(0).future.complete(null);
		buffers.get(1).future.complete(null);
		
		Assert.assertFalse(clientFuture1.isDone());
		Assert.assertFalse(clientFuture2.isDone());
		buffers.get(2).future.complete(null);
		clientFuture1.get(2, TimeUnit.SECONDS);
		clientFuture2.get(2, TimeUnit.SECONDS);
		
		CompletableFuture<Void> fut1 = svrEngine.feedEncryptedPacket(buffers.get(0).engineToSocketData);
		fut1.get(2, TimeUnit.SECONDS);

		CompletableFuture<Void> fut2 = svrEngine.feedEncryptedPacket(buffers.get(1).engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		fut2.get(2, TimeUnit.SECONDS);

		CompletableFuture<Void> fut3 = svrEngine.feedEncryptedPacket(buffers.get(2).engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);

		List<BufferedFuture> toClientBuffers = svrListener.getHandshake();
		
		toClientBuffers.get(0).future.complete(null);
		
		assertFutureJustResolved(fut3, toClientBuffers.get(1));
		
		CompletableFuture<Void> cliFut = clientEngine.feedEncryptedPacket(toClientBuffers.get(0).engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		cliFut.get(2, TimeUnit.SECONDS);
		
		CompletableFuture<Void> cliFut2 = clientEngine.feedEncryptedPacket(toClientBuffers.get(1).engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTED, clientEngine.getConnectionState());
		Assert.assertTrue(clientListener.connected);
		
		cliFut2.get(2, TimeUnit.SECONDS);
	}

	@After
	public void teardown() {
		System.clearProperty("jdk.tls.server.protocols");
		System.clearProperty("jdk.tls.client.protocols");
	}

	private List<ByteBuffer> split(ByteBuffer engineToSocketData) {
		int len = engineToSocketData.remaining() / 2;
		byte[] data = new byte[len];
		byte[] data2 = new byte[engineToSocketData.remaining() - len];
		engineToSocketData.get(data);
		engineToSocketData.get(data2);
		List<ByteBuffer> list = new ArrayList<>();
		list.add(ByteBuffer.wrap(data));
		list.add(ByteBuffer.wrap(data2));
		return list;
	}

	private void assertFutureJustResolved(CompletableFuture<Void> future, BufferedFuture toSend)
			throws InterruptedException, ExecutionException, TimeoutException {
		Assert.assertFalse(future.isDone());
		toSend.future.complete(null);
		future.get(2, TimeUnit.SECONDS);
	}
	
//	@Test
	public void testSplitTwoIntoThreeBasic() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		ByteBuffer b = ByteBuffer.allocate(17000);
		b.put((byte) 1);
		b.put((byte) 2);
		b.position(b.limit()-2); //simulate buffer full of 0's except first 2 and last 2
		b.put((byte) 3);
		b.put((byte) 4);
		b.flip();

		CompletableFuture<Void> future = clientEngine.feedPlainPacket(b);
		List<BufferedFuture> encrypted = clientListener.getEncrypted();
		//results in two ssl packets instead of the one that was fed in..
		Assert.assertEquals(2, encrypted.size());

		encrypted.get(0).future.complete(null);
		Assert.assertFalse(future.isDone());
		encrypted.get(1).future.complete(null);
		future.get(2, TimeUnit.SECONDS);

		int firstSize = encrypted.get(0).engineToSocketData.remaining();
		ByteBuffer single = combine(encrypted);
		List<ByteBuffer> three = split(single, firstSize);

		CompletableFuture<Void> svrFut1 = svrEngine.feedEncryptedPacket(three.get(0));

		CompletableFuture<Void> svrFut2 = svrEngine.feedEncryptedPacket(three.get(1));

		//result in two decrypted as packet was large..
		BufferedFuture toClient = svrListener.getSingleDecrypted();

		CompletableFuture<Void> svrFut3 = svrEngine.feedEncryptedPacket(three.get(2));

		BufferedFuture toClient2 = svrListener.getSingleDecrypted();

		Assert.assertFalse(svrFut1.isDone());
		toClient.future.complete(null);
		svrFut1.get(2, TimeUnit.SECONDS);

		Assert.assertFalse(svrFut2.isDone());
		Assert.assertFalse(svrFut3.isDone());
		toClient2.future.complete(null);
		svrFut2.get(2, TimeUnit.SECONDS);
		svrFut3.get(2, TimeUnit.SECONDS);

		Assert.assertEquals(17000, toClient.engineToSocketData.remaining()+toClient2.engineToSocketData.remaining());
	}

	private ByteBuffer combine(List<BufferedFuture> buffersToSend) {
		int size = 0;
		for(BufferedFuture b : buffersToSend) {
			size += b.engineToSocketData.remaining();
		}
		ByteBuffer buf = ByteBuffer.allocate(size);
		for(BufferedFuture b : buffersToSend) {
			buf.put(b.engineToSocketData);
		}
		
		buf.flip();
		return buf;
	}

	private List<ByteBuffer> split(ByteBuffer byteBuffer, int firstSize) {
		int splitPoint = firstSize / 2;
		byte[] one = new byte[splitPoint];
		byte[] two = new byte[splitPoint+20];
		byteBuffer.get(one);
		byteBuffer.get(two);
		byte[] three = new byte[byteBuffer.remaining()];
		byteBuffer.get(three);

		ByteBuffer buf1 = ByteBuffer.wrap(one);
		ByteBuffer buf2 = ByteBuffer.wrap(two);
		ByteBuffer buf3 = ByteBuffer.wrap(three);
		
		List<ByteBuffer> list = new ArrayList<>();
		list.add(buf1);
		list.add(buf2);
		list.add(buf3);
		
		return list;
	}

}

