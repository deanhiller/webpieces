package org.webpieces.router.impl;

import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;

public abstract class AbstractRouterService implements RoutingService {

	protected boolean started = false;
	
	@Override
	public final void processHttpRequests(Request req, ResponseStreamer responseCb) {
		try {
			if(!started)
				throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
			processHttpRequestsImpl(req, responseCb);
		} catch (Throwable e) {
			responseCb.failure(e);
		}
	}

	protected abstract void processHttpRequestsImpl(Request req, ResponseStreamer responseCb);
}
