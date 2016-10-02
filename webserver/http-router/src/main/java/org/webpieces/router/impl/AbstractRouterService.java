package org.webpieces.router.impl;

import java.util.Map;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.router.api.exceptions.BadRequestException;

public abstract class AbstractRouterService implements RoutingService {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractRouterService.class);
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
		} catch(BadCookieException e) {
			throw e;
		} catch (Throwable e) {
			log.warn("uncaught exception", e);
			responseCb.failureRenderingInternalServerErrorPage(e);
		}
	}

	protected abstract void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb);
	
	@Override
	public String convertToUrl(String routeId, Map<String, String> args) {
		return routeLoader.convertToUrl(routeId, args);
	}
}
