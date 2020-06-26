package org.webpieces.httpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;

public class SocketListener implements HttpSocketListener {

	private static final Logger log = LoggerFactory.getLogger(SocketListener.class);
	
	@Override
	public void socketClosed(HttpSocket socket) {
		log.info("socket closed="+socket);
	}
	
}