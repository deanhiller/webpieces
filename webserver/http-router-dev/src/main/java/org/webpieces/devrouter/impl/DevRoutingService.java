package org.webpieces.devrouter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.model.bldr.data.MasterRouter;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Injector;

public class DevRoutingService extends AbstractRouterService implements RouterService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private static final Consumer<Injector> NO_OP = whatever -> {};
	
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevClassForName classLoader;
	private WebAppMeta routerModule;
	private RouterConfig config;
	private ControllerLoader finder;
	private ServiceCreator serviceCreator;
	private MasterRouter router;

	@Inject
	public DevRoutingService(
			RouteLoader routeConfig, 
			RouterConfig config, 
			MasterRouter router, 
			DevClassForName loader, 
			ControllerLoader finder,
			CookieTranslator cookieTranslator,
			ObjectTranslator objTranslator,
			ServiceCreator serviceCreator
	) {
		super(routeConfig, cookieTranslator, objTranslator);
		this.routeLoader = routeConfig;
		this.config = config;
		this.router = router;
		this.classLoader = loader;
		this.finder = finder;
		this.serviceCreator = serviceCreator;
		this.lastFileTimestamp = config.getMetaFile().lastModified();
	}

	@Override
	public void start() {
		log.info("Starting DEVELOPMENT server with CompilingClassLoader and HotSwap");
		loadOrReload(injector -> runStartupHooks(injector)); 
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}
	
	@Override
	public CompletableFuture<Void> incomingRequestImpl(RequestContext ctx, ResponseStreamer responseCb) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		return router.invoke(ctx, responseCb);
	}
	
//	private class DevErrorRoutes implements ErrorRoutes {
//		private RouterRequest req;
//		public DevErrorRoutes(RouterRequest req) {
//			this.req = req;
//		}
//
//		@Override
//		public NotFoundInfo fetchNotfoundRoute(NotFoundException e) {
//			return fetchNotFoundRoute(e, req);
//		}
//
//		@Override
//		public RouteMeta fetchInternalServerErrorRoute() {
//			return fetchInternalErrorRoute(req);
//		}
//		
//	}
	
//	public NotFoundInfo fetchNotFoundRoute(NotFoundException e, RouterRequest req) {
//		//Production app's notFound route TBD and used in iframe later
//		RouteMeta origMeta = routeLoader.fetchNotFoundRoute(req.domain);
//
//		if(req.queryParams.containsKey("webpiecesShowPage")) {
//			//This is actually a callback from the below code's iframe!!!
//			if(origMeta.getControllerInstance() == null) {
//				finder.loadControllerIntoMetaObject(origMeta, false);
//				finder.loadFiltersIntoMeta(origMeta, origMeta.getFilters(), false);
//			}
//
//			Service<MethodMeta, Action> svc = origMeta.getService222();
//			return new NotFoundInfo(origMeta, svc, req);
//		}
//
//		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
//				+ " something needs a'fixin.  req="+req, e);
//		
//		RouteImpl r = new RouteImpl("/org/webpieces/devrouter/impl/NotFoundController.notFound", RouteType.NOT_FOUND);
//		RouteModuleInfo info = new RouteModuleInfo("", null);
//		RouteMeta meta = new RouteMeta(r, origMeta.getInjector(), info, config.getUrlEncoding());
//		
//		if(meta.getControllerInstance() == null) {
//			finder.loadControllerIntoMetaObject(meta, false);
//			meta.setService(serviceCreator.create());
//		}
//		
//		String reason = "Your route was not found in routes table";
//		if(e != null)
//			reason = e.getMessage();
//		
//		RouterRequest newRequest = new RouterRequest();
//		newRequest.putMultipart("webpiecesError", "Exception message="+reason);
//		newRequest.putMultipart("url", req.relativePath);
//		
//		return new NotFoundInfo(meta, meta.getService222(), newRequest);
//	}
//
//	public RouteMeta fetchInternalErrorRoute(RouterRequest req) {
//		RouteMeta meta = routeLoader.fetchInternalErrorRoute(req.domain);
//		
//		if(meta.getControllerInstance() == null) {
//			finder.loadControllerIntoMetaObject(meta, false);
//			finder.loadFiltersIntoMeta(meta, meta.getFilters(), false);
//		}
//		
//		return meta;
//	}
	/**
	 * Only used with DevRouterConfig which is not on classpath in prod mode
	 * 
	 * @return
	 */
	private boolean reloadIfTextFileChanged() {
		VirtualFile metaTextFile = config.getMetaFile();
		//if timestamp the same, no changes
		if(lastFileTimestamp == metaTextFile.lastModified())
			return false;

		log.info("text file changed so need to reload RouterModules.java implementation");

		routerModule = routeLoader.load(classLoader, NO_OP);
		lastFileTimestamp = metaTextFile.lastModified();
		return true;
	}

	private void reloadIfClassFilesChanged() {
		String routerModuleClassName = routerModule.getClass().getName();
		ClassLoader previousCl = routerModule.getClass().getClassLoader();
		
		Class<?> newClazz = classLoader.clazzForName(routerModuleClassName);
		ClassLoader newClassLoader = newClazz.getClassLoader();
		if(previousCl == newClassLoader)
			return;
		
		log.info("classloader change so we need to reload all router classes");
		loadOrReload(NO_OP);
	}

	private void loadOrReload(Consumer<Injector> startupHook) {
		routerModule = routeLoader.load(classLoader, startupHook);
	}

}
