package org.webpieces.webserver.api;

import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.nio.api.channels.TCPServerChannel;

public interface WebServer {

	StreamListener start();

	void stop();

	TCPServerChannel getUnderlyingHttpChannel();

	TCPServerChannel getUnderlyingHttpsChannel();
	
}
