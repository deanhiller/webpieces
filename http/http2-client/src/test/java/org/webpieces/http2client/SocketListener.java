package org.webpieces.http2client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;

public class SocketListener implements Http2SocketListener {

	private static final Logger log = LoggerFactory.getLogger(SocketListener.class);
	
	@Override
	public void socketFarEndClosed(Http2Socket socket) {
		log.info("Socket far end closed");
	}

}
