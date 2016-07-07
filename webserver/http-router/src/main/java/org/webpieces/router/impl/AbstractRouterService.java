package org.webpieces.router.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouterRequest;

public abstract class AbstractRouterService implements RoutingService {
	
	protected boolean started = false;
	
	@Override
	public final void processHttpRequests(RouterRequest req, ResponseStreamer responseCb) {
		try {
			if(!started)
				throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
			processHttpRequestsImpl(req, responseCb);
		} catch (Throwable e) {
			responseCb.failure(e);
		}
	}

	protected abstract void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb);
	
}
