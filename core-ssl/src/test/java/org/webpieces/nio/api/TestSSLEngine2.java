package org.webpieces.nio.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLEngine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.webpieces.ssl.api.Action;
import org.webpieces.ssl.api.ActionState;
import org.webpieces.ssl.api.AsyncSSLEngine;
import org.webpieces.ssl.api.AsyncSSLFactory;
import org.webpieces.ssl.api.ConnectionState;
import org.webpieces.ssl.api.SslMemento;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;

public class TestSSLEngine2 {

	private SslMemento clientMemento;
	private SslMemento svrMemento;
	private AsyncSSLEngine engine;

	@Before
	public void setup() throws GeneralSecurityException, IOException {
		MockSSLEngineFactory sslEngineFactory = new MockSSLEngineFactory();	
		BufferPool pool = new BufferCreationPool(false, 17000, 1000);
		engine = AsyncSSLFactory.createParser(pool);

		SSLEngine client = sslEngineFactory.createEngineForSocket();
		SSLEngine svr = sslEngineFactory.createEngineForServerSocket();
		
		clientMemento = engine.createMemento("id", client);
		Assert.assertEquals(ConnectionState.NOT_STARTED, clientMemento.getConnectionState());
		clientMemento = engine.beginHandshake(clientMemento);
		Assert.assertEquals(ConnectionState.CONNECTING, clientMemento.getConnectionState());
		
		Action action = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.SEND_TO_SOCKET, action.getActionState());
		ByteBuffer buffer = action.getToSendToSocket().get(0);

		svrMemento = engine.createMemento("svr", svr);
		Assert.assertEquals(ConnectionState.NOT_STARTED, svrMemento.getConnectionState());
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffer);
		Assert.assertEquals(ConnectionState.CONNECTING, svrMemento.getConnectionState());
		
		Action actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, actionSvr.getActionState());
		actionSvr.getRunnableToRun().run();
		
		svrMemento = engine.runnableComplete(svrMemento);
		Assert.assertEquals(ConnectionState.CONNECTING, svrMemento.getConnectionState());
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.SEND_TO_SOCKET, actionSvr.getActionState());
		
		ByteBuffer buf = actionSvr.getToSendToSocket().get(0);
		clientMemento = engine.feedEncryptedPacket(clientMemento, buf);
		
		Assert.assertEquals(ConnectionState.CONNECTING, clientMemento.getConnectionState());
		action = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, action.getActionState());
		action.getRunnableToRun().run();
		
		clientMemento = engine.runnableComplete(clientMemento);
		Assert.assertEquals(ConnectionState.CONNECTING, clientMemento.getConnectionState());
		Assert.assertEquals(ActionState.SEND_TO_SOCKET, clientMemento.getActionToTake().getActionState());		
	}
	
	@Test
	public void testBasic() throws GeneralSecurityException, IOException {
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(0));
		Action actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, actionSvr.getActionState());
		actionSvr.getRunnableToRun().run();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(1));
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, actionSvr.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(2));
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CONNECTED_AND_SEND_TO_SOCKET, actionSvr.getActionState());
		List<ByteBuffer> toClientBuffers = actionSvr.getToSendToSocket();
		
		clientMemento = engine.feedEncryptedPacket(clientMemento, toClientBuffers.get(0));
		Assert.assertEquals(ConnectionState.CONNECTING, clientMemento.getConnectionState());
		Action actionClient = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, actionClient.getActionState());
		
		clientMemento = engine.feedEncryptedPacket(clientMemento, toClientBuffers.get(1));
		Assert.assertEquals(ConnectionState.CONNECTED, clientMemento.getConnectionState());
		actionClient = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.CONNECTED, actionClient.getActionState());
		
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
		
		clientMemento = engine.feedPlainPacket(clientMemento, b);
		Action action = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.SEND_TO_SOCKET, action.getActionState());
		//results in two ssl packets instead of the one that was fed in..
		Assert.assertEquals(2, action.getToSendToSocket().size());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, action.getToSendToSocket().get(0));
		Action svrAction = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.SEND_TO_CLIENT, svrAction.getActionState());
		ByteBuffer buffer = svrAction.getToSendToClient().get(0);
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, action.getToSendToSocket().get(1));
		svrAction = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.SEND_TO_CLIENT, svrAction.getActionState());
		ByteBuffer buffer2 = svrAction.getToSendToClient().get(0);
		
		Assert.assertEquals(17000, buffer.remaining()+buffer2.remaining());
	}

	@Test
	public void testCombineBuffers() {
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		ByteBuffer combine = combine(buffers);
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, combine);
		Action actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, actionSvr.getActionState());
		actionSvr.getRunnableToRun().run();
		
		svrMemento = engine.runnableComplete(svrMemento);
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ConnectionState.CONNECTED, svrMemento.getConnectionState());
		Assert.assertEquals(ActionState.CONNECTED_AND_SEND_TO_SOCKET, actionSvr.getActionState());
		List<ByteBuffer> toClientBuffers = actionSvr.getToSendToSocket();
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
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(0));
		Action actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, actionSvr.getActionState());
		Runnable run = actionSvr.getRunnableToRun();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(1));
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.WAITING_ON_RUNNABLE_COMPLETE_CALL, actionSvr.getActionState());
		
		try {
			engine.runnableComplete(svrMemento);
			Assert.fail("should have thrown an exception");
		} catch(IllegalStateException e) {}
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(2));
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.WAITING_ON_RUNNABLE_COMPLETE_CALL, actionSvr.getActionState());		
		
		run.run();
		
		svrMemento = engine.runnableComplete(svrMemento);
		actionSvr = svrMemento.getActionToTake();
		Assert.assertEquals(ConnectionState.CONNECTED, svrMemento.getConnectionState());
		Assert.assertEquals(ActionState.CONNECTED_AND_SEND_TO_SOCKET, actionSvr.getActionState());
		List<ByteBuffer> toClientBuffers = actionSvr.getToSendToSocket();
	}
	
	@Test
	public void testHalfThenTooMuchFedInPacket() {
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		List<ByteBuffer> first = split(buffers.get(0));
		List<ByteBuffer> second = split(buffers.get(1));
		ByteBuffer halfAndHalf = combine(first.get(1), second.get(0));
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, first.get(0));
		Action action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, action.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, halfAndHalf);
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, action.getActionState());
		Runnable run = action.getRunnableToRun();
		run.run();
		
		svrMemento = engine.runnableComplete(svrMemento);
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, action.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, second.get(1));
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, action.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(2));
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CONNECTED_AND_SEND_TO_SOCKET, action.getActionState());		
	}

	@Test
	public void testHalfThenTooMuchFedInPacketAndRunnableDelayed() {
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		List<ByteBuffer> first = split(buffers.get(0));
		List<ByteBuffer> second = split(buffers.get(1));
		ByteBuffer halfAndHalf = combine(first.get(1), second.get(0));
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, first.get(0));
		Action action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, action.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, halfAndHalf);
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.RUN_RUNNABLE, action.getActionState());
		Runnable run = action.getRunnableToRun();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, second.get(1));
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.WAITING_ON_RUNNABLE_COMPLETE_CALL, action.getActionState());
		
		run.run();
		
		svrMemento = engine.runnableComplete(svrMemento);
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.NOT_ENOUGH_ENCRYPTED_BYTES_YET, action.getActionState());
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(2));
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CONNECTED_AND_SEND_TO_SOCKET, action.getActionState());		
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
