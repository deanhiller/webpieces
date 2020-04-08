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
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.ssl.api.MockSslListener.BufferedFuture;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class TestHandshakeBackpressure {

	private AsyncSSLEngine clientEngine;
	private AsyncSSLEngine svrEngine;
	private MockSslListener clientListener = new MockSslListener();
	private MockSslListener svrListener = new MockSslListener();
	private List<BufferedFuture> buffers;

	@Before
	public void setup() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		System.setProperty("jdk.tls.server.protocols", "TLSv1.2");
		System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

		SSLMetrics metrics = new SSLMetrics("", new SimpleMeterRegistry());
		MockSSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		SSLEngine client = sslEngineFactory.createEngineForSocket();
		SSLEngine svr = sslEngineFactory.createEngineForServerSocket();
		clientEngine = AsyncSSLFactory.create("client", client, pool, clientListener, metrics);
		svrEngine = AsyncSSLFactory.create("svr", svr, pool, svrListener, metrics);
		
		Assert.assertEquals(ConnectionState.NOT_STARTED, clientEngine.getConnectionState());
		Assert.assertEquals(ConnectionState.NOT_STARTED, svrEngine.getConnectionState());
		
		CompletableFuture<Void> cliFuture = clientEngine.beginHandshake();
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		BufferedFuture toSend = clientListener.getSingleHandshake();
		
		assertFutureJustResolved(cliFuture, toSend);
		
		CompletableFuture<Void> svrFuture = svrEngine.feedEncryptedPacket(toSend.engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		
		Assert.assertEquals(ConnectionState.CONNECTING, svrEngine.getConnectionState());
		BufferedFuture toSend2 = svrListener.getSingleHandshake();

		assertFutureJustResolved(svrFuture, toSend2);
		
		CompletableFuture<Void> clientFuture = clientEngine.feedEncryptedPacket(toSend2.engineToSocketData);
		Assert.assertEquals(ConnectionState.CONNECTING, clientEngine.getConnectionState());
		
		buffers = clientListener.getHandshake();
		buffers.get(0).future.complete(null);
		buffers.get(1).future.complete(null);
		
		assertFutureJustResolved(clientFuture, buffers.get(2));
	}

	@After
	public void teardown() {
		System.clearProperty("jdk.tls.server.protocols");
		System.clearProperty("jdk.tls.client.protocols");
	}

	private void assertFutureJustResolved(CompletableFuture<Void> future, BufferedFuture toSend)
			throws InterruptedException, ExecutionException, TimeoutException {
		Assert.assertFalse(future.isDone());
		toSend.future.complete(null);
		future.get(2, TimeUnit.SECONDS);
	}
	
	@Test
	public void testTransferTwoPacketsForOneEncrypted() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		finishHandshake();
		
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
		
		CompletableFuture<Void> svrFut1 = svrEngine.feedEncryptedPacket(encrypted.get(0).engineToSocketData);
		BufferedFuture toSendToClient = svrListener.getSingleDecrypted();
		
		Assert.assertFalse(svrFut1.isDone());
		toSendToClient.future.complete(null);
		svrFut1.get(2, TimeUnit.SECONDS);

		CompletableFuture<Void> svrFut2 = svrEngine.feedEncryptedPacket(encrypted.get(1).engineToSocketData);
		BufferedFuture toSendToClient2 = svrListener.getSingleDecrypted();

		Assert.assertFalse(svrFut2.isDone());
		toSendToClient2.future.complete(null);
		svrFut2.get(2, TimeUnit.SECONDS);
		
		Assert.assertEquals(17000, toSendToClient.engineToSocketData.remaining()+toSendToClient2.engineToSocketData.remaining());
	}
	
	@Test
	public void testTransferOnePacketsForOneBigOneEncrypted() throws GeneralSecurityException, IOException, InterruptedException, ExecutionException, TimeoutException {
		finishHandshake();
		
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
		
		ByteBuffer single = combine(encrypted);
		CompletableFuture<Void> svrFut1 = svrEngine.feedEncryptedPacket(single);
		//result in two decrypted as packet was large..
		List<BufferedFuture> toClient = svrListener.getDecrypted();
		
		toClient.get(0).future.complete(null);
		
		Assert.assertFalse(svrFut1.isDone());
		toClient.get(1).future.complete(null);
		svrFut1.get(2, TimeUnit.SECONDS);
		
		Assert.assertEquals(17000, toClient.get(0).engineToSocketData.remaining()+toClient.get(1).engineToSocketData.remaining());
	}
	
	private void finishHandshake() throws InterruptedException, ExecutionException, TimeoutException {
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

	@Test
	public void testCombineBuffers() throws InterruptedException, ExecutionException, TimeoutException {
		ByteBuffer combine = combine(buffers);
		
		CompletableFuture<Void> svrFut = svrEngine.feedEncryptedPacket(combine);
		
		List<BufferedFuture> toClientBuffers = svrListener.getHandshake();
		
		toClientBuffers.get(0).future.complete(null);

		Assert.assertFalse(svrFut.isDone());
		toClientBuffers.get(1).future.complete(null);
		svrFut.get(2, TimeUnit.SECONDS);
		
		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);
		
		ByteBuffer toCli = combine(toClientBuffers);
		CompletableFuture<Void> cliFut2 = clientEngine.feedEncryptedPacket(toCli);
		Assert.assertEquals(ConnectionState.CONNECTED, clientEngine.getConnectionState());
		Assert.assertTrue(clientListener.connected);
		
		cliFut2.get(2, TimeUnit.SECONDS);
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

	
	@Test
	public void testHalfThenTooMuchFedInPacket() throws InterruptedException, ExecutionException, TimeoutException {
		List<ByteBuffer> first = split(buffers.get(0).engineToSocketData);
		List<ByteBuffer> second = split(buffers.get(1).engineToSocketData);
		ByteBuffer halfAndHalf = combine(first.get(1), second.get(0));
		
		CompletableFuture<Void> future1 = svrEngine.feedEncryptedPacket(first.get(0));
		Assert.assertEquals(0, svrListener.getHandshake().size());
		Assert.assertFalse(future1.isDone());

		CompletableFuture<Void> future2 = svrEngine.feedEncryptedPacket(halfAndHalf);
		Assert.assertEquals(0, svrListener.getHandshake().size());
		future1.get(2, TimeUnit.SECONDS);
		Assert.assertFalse(future2.isDone());
		
		CompletableFuture<Void> future3 = svrEngine.feedEncryptedPacket(second.get(1));
		Assert.assertEquals(0, svrListener.getHandshake().size());
		future2.get(2, TimeUnit.SECONDS);
		future3.get(2, TimeUnit.SECONDS);

		CompletableFuture<Void> future4 = svrEngine.feedEncryptedPacket(buffers.get(2).engineToSocketData);

		List<BufferedFuture> buf = svrListener.getHandshake();

		Assert.assertEquals(ConnectionState.CONNECTED, svrEngine.getConnectionState());
		Assert.assertTrue(svrListener.connected);

		buf.get(0).future.complete(null);
		
		Assert.assertFalse(future4.isDone());
		buf.get(1).future.complete(null);		
		future4.get(2, TimeUnit.SECONDS);		
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

