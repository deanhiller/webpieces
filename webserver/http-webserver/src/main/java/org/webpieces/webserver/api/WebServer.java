package org.webpieces.webserver.api;

import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface WebServer {

	void startSync();

	XFuture<Void> startAsync();

	void stop();

	TCPServerChannel getUnderlyingHttpChannel();

	TCPServerChannel getUnderlyingHttpsChannel();
	
}
