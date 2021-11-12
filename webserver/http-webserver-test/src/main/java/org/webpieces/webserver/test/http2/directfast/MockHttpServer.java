package org.webpieces.webserver.test.http2.directfast;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.frontend2.api.HttpServer;
import org.webpieces.frontend2.api.HttpSvrConfig;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.webserver.test.MockServerChannel;

public class MockHttpServer implements HttpServer {

	private HttpSvrConfig config;

	public MockHttpServer(HttpSvrConfig config) {
		this.config = config;
	}

	@Override
	public XFuture<Void> start() {
		return XFuture.completedFuture(null);
	}

	@Override
	public XFuture<Void> close() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableOverloadMode(ByteBuffer overloadResponse) {
	}

	@Override
	public void disableOverloadMode() {
	}

	@Override
	public TCPServerChannel getUnderlyingChannel() {
		return new MockServerChannel(config);
	}

}
