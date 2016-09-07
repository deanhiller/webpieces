package org.webpieces.devrouter.impl;

import java.util.ArrayList;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.ErrorRoutes;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.NotFoundInfo;
import org.webpieces.router.impl.RouteImpl;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.RouteModuleInfo;
import org.webpieces.util.file.VirtualFile;

public class DevRoutingService extends AbstractRouterService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevClassForName classLoader;
	private WebAppMeta routerModule;
	private RouterConfig config;

	@Inject
	public DevRoutingService(RouteLoader routeConfig, RouterConfig config, DevClassForName loader) {
		super(routeConfig);
		this.routeLoader = routeConfig;
		this.config = config;
		this.classLoader = loader;
		this.lastFileTimestamp = config.getMetaFile().lastModified();
	}

	@Override
	public void start() {
		log.info("Starting DEVELOPMENT server with CompilingClassLoader and HotSwap");
		loadOrReload();
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}
	
	@Override
	public void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		MatchResult result = routeLoader.fetchRoute(req);
		
		RouteMeta meta = result.getMeta();
		if(meta.getRoute().getRouteType() == RouteType.STATIC) {
			//RESET the encodings to known so we don't try to go the compressed cache which doesn't
			//exist in dev server since we want the latest files always
			req.encodings = new ArrayList<>();
		} else if(meta.getControllerInstance() == null) {
			routeLoader.loadControllerIntoMetaObject(meta, false);
		}
		
		routeLoader.invokeRoute(result, req, responseCb, new DevErrorRoutes(req)); 
	}
	
	private class DevErrorRoutes implements ErrorRoutes {
		private RouterRequest req;
		public DevErrorRoutes(RouterRequest req) {
			this.req = req;
		}

		@Override
		public NotFoundInfo fetchNotfoundRoute(NotFoundException e) {
			return fetchNotFoundRoute(e, req);
		}

		@Override
		public MatchResult fetchInternalServerErrorRoute() {
			return fetchInternalErrorRoute(req);
		}
		
	}
	
	public NotFoundInfo fetchNotFoundRoute(NotFoundException e, RouterRequest req) {
		//Production app's notFound route TBD and used in iframe later
		MatchResult origResult = routeLoader.fetchNotFoundRoute();
		RouteMeta origMeta = origResult.getMeta();

		if(req.queryParams.containsKey("webpiecesShowPage")) {
			//This is actually a callback from the below code's iframe!!!
			if(origMeta.getControllerInstance() == null)
				routeLoader.loadControllerIntoMetaObject(origMeta, false);

			MatchResult result = new MatchResult(origMeta);
			return new NotFoundInfo(result, req);
		}

		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
				+ " something needs a'fixin.  req="+req, e);
		
		RouteImpl r = new RouteImpl("/org/webpieces/devrouter/impl/NotFoundController.notFound", RouteType.NOT_FOUND);
		RouteModuleInfo info = new RouteModuleInfo("", null);
		RouteMeta meta = new RouteMeta(r, origMeta.getInjector(), info, config.getUrlEncoding());
		MatchResult result = new MatchResult(meta);
		
		if(meta.getControllerInstance() == null) {
			routeLoader.loadControllerIntoMetaObject(meta, false);
		}
		
		String reason = "Your route was not found in routes table";
		if(e != null)
			reason = e.getMessage();
		
		RouterRequest newRequest = new RouterRequest();
		newRequest.multiPartFields.put("webpiecesError", "Exception message="+reason);
		newRequest.multiPartFields.put("url", req.relativePath);
		
		return new NotFoundInfo(result, newRequest);
	}

	public MatchResult fetchInternalErrorRoute(RouterRequest req) {
		MatchResult result = routeLoader.fetchInternalErrorRoute();
		
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			routeLoader.loadControllerIntoMetaObject(meta, false);
		}
		
		return result;
	}
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

		routerModule = routeLoader.load(classLoader);
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
		loadOrReload();
	}

	private void loadOrReload() {
		routerModule = routeLoader.load(classLoader);		
	}
}
