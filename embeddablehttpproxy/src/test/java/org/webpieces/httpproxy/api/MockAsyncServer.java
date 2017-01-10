package org.webpieces.httpproxy.api;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncServer;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class MockAsyncServer implements AsyncServer {

	@Override
	public void start(SocketAddress bindAddr) {
		// TODO Auto-generated method stub

	}

	@Override
	public CompletableFuture<Void> closeServerChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableOverloadMode() {
		// TODO Auto-generated method stub

	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		// TODO Auto-generated method stub
		return null;
	}

}
