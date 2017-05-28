package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;

public class RequestReceiver implements StreamListener {
	
	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	@Inject
	private RequestHelpFacade facade;
	
	@Override
	public HttpStream openStream() {
		return new WebpiecesStreamHandle(facade);
	}

}
