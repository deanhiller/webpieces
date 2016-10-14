package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class MockHttpServerSocket implements HttpServerSocket {

	@Override
	public CompletableFuture<Void> closeSocket() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
	}

	@Override
	public void disableOverloadMode() {
	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		return null;
	}

}
