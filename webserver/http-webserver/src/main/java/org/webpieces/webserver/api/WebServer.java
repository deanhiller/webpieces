package org.webpieces.webserver.api;

import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.nio.api.channels.TCPServerChannel;

public interface WebServer {

	RequestListener start();

	void stop();

	TCPServerChannel getUnderlyingHttpChannel();

	TCPServerChannel getUnderlyingHttpsChannel();
	
}
