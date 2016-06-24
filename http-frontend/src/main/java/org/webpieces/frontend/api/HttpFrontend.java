package org.webpieces.frontend.api;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface HttpFrontend {

	void close();

	void enableOverloadMode(ByteBuffer overloadResponse);

	void disableOverloadMode();

	TCPServerChannel getUnderlyingChannel();

}
