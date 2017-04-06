package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServer;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class MockHttpServer implements HttpServer {

	private FrontendConfig config;
	private MockServerChannel channel = new MockServerChannel();
	
	public MockHttpServer(FrontendConfig config) {
		this.config = config;
	}

	@Override
	public void start() {
		channel.bind(config.bindAddress);
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
		return channel;
	}

}
