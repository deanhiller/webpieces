package org.webpieces.webserver.test.http2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;

public class NullCloseListener implements Http2SocketListener {

	private static final Logger log = LoggerFactory.getLogger(NullCloseListener.class);
	
	@Override
	public void socketFarEndClosed(Http2Socket socket) {
		log.info("socket far end closed="+socket);
	}

}
