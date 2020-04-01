package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.routers.AMasterRouter;
import org.webpieces.util.cmdline2.Arguments;

import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProdRouterService extends AbstractRouterService {

	private static final Logger log = LoggerFactory.getLogger(ProdRouterService.class);
	
	private RouteLoader routeLoader;
	private ClassForName loader;
	private AMasterRouter router;
	
	@Inject
	public ProdRouterService(
			RouteLoader routeLoader, 
			CookieTranslator cookieTranslator, 
			ObjectTranslator translator, 
			ProdClassForName loader,
			AMasterRouter router,
			ApplicationContext appContext
	) {
		super(appContext, routeLoader, cookieTranslator, translator);
		this.routeLoader = routeLoader;
		this.loader = loader;
		this.router = router;
	}

	@Override
	public void configure(Arguments arguments) {
		routeLoader.configure(loader, arguments);
	}
	
	//add Route HOOK callback so translate RouteId -> route and route->controller.method to call
	@Override
	public Injector start() {
		log.info("Starting PROD server with NO compiling classloader");
		
		Injector inj = routeLoader.load(injector -> runStartupHooks(injector));
		started = true;
		return inj;
	}

	@Override
	public void stop() {
		started = false;
	}

	@Override
	public CompletableFuture<Void> incomingRequestImpl(RequestContext ctx, ResponseStreamer responseCb) {
		if(log.isDebugEnabled())
			router.printAllRoutes();
	
		return router.invoke(ctx, responseCb);

	}

//	//This only exists so dev mode can swap it out and load error routes dynamically as code changes..
//	private static class ProdErrorRoutes implements ErrorRoutes {
//		private RouteLoader routeLoader;
//		private RouterRequest req;
//		public ProdErrorRoutes(RouterRequest req, RouteLoader routeLoader) {
//			this.req = req;
//			this.routeLoader = routeLoader;
//		}
//
//		public NotFoundInfo fetchNotfoundRoute(NotFoundException e) {
//			//not found is normal in prod mode so we don't log that and only log warnings in dev mode
//			RouteMeta result = routeLoader.fetchNotFoundRoute(req.domain);
//
//			//every request for not found route must apply filters(unlike other routes).  There are tests
//			//for this use case with the LoginFitler in TestHttps
//			Service<MethodMeta, Action> svc = routeLoader.createNotFoundService(result, req);
//			
//			return new NotFoundInfo(result, svc, req);
//		}
//		
//		public RouteMeta fetchInternalServerErrorRoute() {
//			return routeLoader.fetchInternalErrorRoute(req.domain);
//		}
//	}

}
