package org.webpieces.webserver.impl;

import org.webpieces.frontend2.api.HttpStream;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.router.api.RouterResponseHandler;
import org.webpieces.router.api.RouterService;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.StreamRef;

public class WebpiecesStreamHandle implements HttpStream {

	private RouterService routingService;

	public WebpiecesStreamHandle(RouterService routingService) {
		this.routingService = routingService;
	}

	@Override
	public StreamRef incomingRequest(Http2Request headers, ResponseStream stream) {
		RouterResponseHandler handler = new RouterResponseHandlerImpl(stream);
		StreamRef ref = routingService.incomingRequest(headers, handler);
		return ref;
	}

}
