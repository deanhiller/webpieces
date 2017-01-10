package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.HttpServer;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class MockHttpServer implements HttpServer {

	@Override
	public void start() {
	}
	
	@Override
	public CompletableFuture<Void> close() {
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
