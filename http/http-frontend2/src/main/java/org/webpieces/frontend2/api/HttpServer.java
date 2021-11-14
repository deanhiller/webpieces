package org.webpieces.frontend2.api;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface HttpServer {

	XFuture<Void> start();
	
	XFuture<Void> close();

	void enableOverloadMode(ByteBuffer overloadResponse);

	void disableOverloadMode();

	TCPServerChannel getUnderlyingChannel();

}
