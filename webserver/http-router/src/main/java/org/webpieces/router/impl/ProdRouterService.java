package org.webpieces.router.impl;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.loader.ProdClassForName;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.routers.ARouter;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.futures.FutureHelper;

import com.google.inject.Injector;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

@Singleton
public class ProdRouterService extends AbstractRouterService {

	private static final Logger log = LoggerFactory.getLogger(ProdRouterService.class);
	
	private RouteLoader routeLoader;
	private ClassForName loader;
	private ARouter router;
	
	@Inject
	public ProdRouterService(
			RouteLoader routeLoader, 
			CookieTranslator cookieTranslator, 
			ObjectTranslator translator, 
			ProdClassForName loader,
			ARouter router,
			WebInjector webInjector,
			FutureHelper futureUtil
	) {
		super(webInjector, routeLoader, cookieTranslator, translator);
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
		return inj;
	}

	@Override
	public RouterStreamRef incomingRequestImpl(RequestContext ctx, ProxyStreamHandle handler) {
		if(log.isDebugEnabled())
			router.printAllRoutes();
	
		return router.invoke(ctx, handler);
	}

}
