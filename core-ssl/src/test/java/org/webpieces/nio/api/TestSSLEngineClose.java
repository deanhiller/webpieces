package org.webpieces.nio.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
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

public class TestSSLEngineClose {

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
		
		List<ByteBuffer> buffers = clientMemento.getActionToTake().getToSendToSocket();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buffers.get(0));
		actionSvr = svrMemento.getActionToTake();
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
	
	@Test
	public void testBasicCloseFromServer() throws GeneralSecurityException, IOException {
		svrMemento = engine.close(svrMemento);
		Action action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED_AND_SEND_TO_SOCKET, action.getActionState());
		List<ByteBuffer> bufs = action.getToSendToSocket();
		
		clientMemento = engine.feedEncryptedPacket(clientMemento, bufs.get(0));
		Action clientAction = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED_AND_SEND_TO_SOCKET, clientAction.getActionState());
		ByteBuffer buf = clientAction.getToSendToSocket().get(0);
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, buf);
		action = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED, action.getActionState());
	}
	
	@Test
	public void testBasicCloseFromClient() throws GeneralSecurityException, IOException {
		clientMemento = engine.close(clientMemento);
		Action action = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED_AND_SEND_TO_SOCKET, action.getActionState());
		List<ByteBuffer> bufs = action.getToSendToSocket();
		
		svrMemento = engine.feedEncryptedPacket(svrMemento, bufs.get(0));
		Action clientAction = svrMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED_AND_SEND_TO_SOCKET, clientAction.getActionState());
		ByteBuffer buf = clientAction.getToSendToSocket().get(0);
		
		clientMemento = engine.feedEncryptedPacket(clientMemento, buf);
		action = clientMemento.getActionToTake();
		Assert.assertEquals(ActionState.CLOSED, action.getActionState());
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

}
