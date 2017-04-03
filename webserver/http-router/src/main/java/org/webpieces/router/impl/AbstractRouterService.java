package org.webpieces.router.impl;

import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.Startable;
import org.webpieces.router.api.exceptions.BadCookieException;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public abstract class AbstractRouterService implements RoutingService {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractRouterService.class);
	protected boolean started = false;
	private RouteLoader routeLoader;
	
	public AbstractRouterService(RouteLoader routeLoader) {
		this.routeLoader = routeLoader;
	}

	@Override
	public final void incomingCompleteRequest(RouterRequest req, ResponseStreamer responseCb) {
		try {
			if(!started)
				throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
			incomingRequestImpl(req, responseCb);
		} catch(BadCookieException e) {
			throw e;
		} catch (Throwable e) {
			log.warn("uncaught exception", e);
			responseCb.failureRenderingInternalServerErrorPage(e);
		}
	}

	protected abstract void incomingRequestImpl(RouterRequest req, ResponseStreamer responseCb);
	
	@Override
	public String convertToUrl(String routeId, Map<String, String> args, boolean isValidating) {
		return routeLoader.convertToUrl(routeId, args, isValidating);
	}
	
	@Override
	public FileMeta relativeUrlToHash(String urlPath) {
		if(!urlPath.startsWith("/"))
			urlPath = "/"+urlPath;
		
		return routeLoader.relativeUrlToHash(urlPath);
	}
	
	protected void runStartupHooks(Injector injector) {
		log.info("Running startup hooks for server");
		
		Key<Set<Startable>> key = Key.get(new TypeLiteral<Set<Startable>>(){});
		Set<Startable> startupHooks = injector.getInstance(key);
		for(Startable s : startupHooks) {
			runStartupHook(s);
		}
		log.info("Ran all startup hooks");
	}

	private void runStartupHook(Startable s) {
		try {
			log.info("starting startup hook="+s.getClass().getSimpleName());
			s.start();
			log.info("Successfully ran startup hook="+s.getClass().getSimpleName());
		} catch(Throwable e) {
			throw new RuntimeException("Startup hook="+s.getClass().getSimpleName()+" failed", e);
		}
	}
}
