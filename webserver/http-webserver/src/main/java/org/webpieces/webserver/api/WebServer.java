package org.webpieces.webserver.api;

import org.webpieces.frontend.api.RequestListener;
import org.webpieces.nio.api.channels.TCPServerChannel;

public interface WebServer {

	RequestListener start();

	void stop();

	TCPServerChannel getUnderlyingHttpChannel();

	TCPServerChannel getUnderlyingHttpsChannel();
	
}
