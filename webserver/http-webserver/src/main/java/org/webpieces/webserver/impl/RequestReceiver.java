package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ServerSocketInfo;

import com.webpieces.http2engine.api.StreamHandle;

public class RequestReceiver implements HttpRequestListener {
	
	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	@Inject
	private RequestHelpFacade facade;
	
	@Override
	public HttpStream openStream() {
		return new WebpiecesStreamHandle(facade);
	}

}
