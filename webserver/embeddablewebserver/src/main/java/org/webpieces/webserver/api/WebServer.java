package org.webpieces.webserver.api;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface WebServer {

	void start();

	void stop();

	TCPServerChannel getUnderlyingHttpChannel();

	TCPServerChannel getUnderlyingHttpsChannel();
}
