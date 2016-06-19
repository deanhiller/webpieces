package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.RoutingService;

public class RequestReceiver implements HttpRequestListener {

	@Inject
	private RoutingService routingService;
	
	@Override
	public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		
		
	}

	@Override
	public void sendServerResponse(FrontendSocket channel, Throwable exc, KnownStatusCode status) {
		
	}

	@Override
	public void clientClosedChannel(FrontendSocket channel) {
	}

	@Override
	public void applyWriteBackPressure(FrontendSocket channel) {
	}

	@Override
	public void releaseBackPressure(FrontendSocket channel) {
	}

}
