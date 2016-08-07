package org.webpieces.router.impl;

import java.util.Map;

import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RouterRequest;

public abstract class AbstractRouterService implements RoutingService {
	
	protected boolean started = false;
	private RouteLoader routeLoader;
	
	public AbstractRouterService(RouteLoader routeLoader) {
		this.routeLoader = routeLoader;
	}

	@Override
	public final void processHttpRequests(RouterRequest req, ResponseStreamer responseCb) {
		try {
			if(!started)
				throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
			processHttpRequestsImpl(req, responseCb);
		} catch (Throwable e) {
			responseCb.failureRenderingInternalServerErrorPage(e);
		}
	}

	protected abstract void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb);
	
	@Override
	public String convertToUrl(String routeId, Map<String, String> args) {
		return routeLoader.convertToUrl(routeId, args);
	}
}
