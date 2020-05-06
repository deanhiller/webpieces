package org.webpieces.webserver.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;

@Singleton
public class RequestReceiver implements StreamListener {
	
	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	private final RequestHelpFacade facade;
	
	@Inject
	public RequestReceiver(RequestHelpFacade facade) {
		super();
		this.facade = facade;
	}

	@Override
	public HttpStream openStream(FrontendSocket socket) {
		return new WebpiecesStreamHandle(facade);
	}

	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
		//TODO: ((dhiller) fill this in calling RequestHelpFacade or something?
		
	}

}
