package org.webpieces.webserver.test;

import java.nio.ByteBuffer;

import org.webpieces.frontend.api.HttpFrontend;
import org.webpieces.nio.api.channels.TCPServerChannel;

public class MockHttpFrontend implements HttpFrontend {

	@Override
	public void close() {
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
