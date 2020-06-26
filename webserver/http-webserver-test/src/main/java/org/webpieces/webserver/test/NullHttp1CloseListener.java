package org.webpieces.webserver.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpclient11.api.HttpSocketListener;

public class NullHttp1CloseListener implements HttpSocketListener {

	private static final Logger log = LoggerFactory.getLogger(NullHttp1CloseListener.class);
	
	@Override
	public void socketClosed(HttpSocket socket) {
		log.info("Far end closed socket="+socket);
	}

}
