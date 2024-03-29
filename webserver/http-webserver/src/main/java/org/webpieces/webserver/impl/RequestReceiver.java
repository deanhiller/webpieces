package org.webpieces.webserver.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.nio.api.Throttle;
import org.webpieces.nio.impl.cm.basic.Throttler;
import org.webpieces.router.api.RouterService;

@Singleton
public class RequestReceiver implements StreamListener {
	private final Throttle throttler;

	//private static final Logger log = LoggerFactory.getLogger(RequestReceiver.class);
	
	private RouterService service;

	@Inject
	public RequestReceiver(RouterService service, Throttle throttler) {
		super();
		this.service = service;
		this.throttler = throttler;
	}

	@Override
	public HttpStream openStream(FrontendSocket socket) {
		return new WebpiecesStreamHandle(service, throttler);
	}

	@Override
	public void fireIsClosed(FrontendSocket socketThatClosed) {
		//TODO: ((dhiller) fill this in calling RequestHelpFacade or something?
		
	}

}
