package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.frontend2.api.FrontendStream;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;

import com.webpieces.http2engine.api.StreamHandle;

public class RequestReceiver implements HttpRequestListener {
	
	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	@Inject
	private RequestHelpFacade facade;
	
	@Override
	public StreamHandle openStream(FrontendStream stream, SocketInfo info) {
		return new WebpiecesStreamHandle(facade, stream, info);
	}

}
