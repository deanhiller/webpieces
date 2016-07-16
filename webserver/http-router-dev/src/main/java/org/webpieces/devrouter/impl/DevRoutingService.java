package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.ErrorRoutes;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.util.file.VirtualFile;

public class DevRoutingService extends AbstractRouterService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevClassForName classLoader;
	private WebAppMeta routerModule;
	private HttpRouterConfig config;

	@Inject
	public DevRoutingService(RouteLoader routeConfig, HttpRouterConfig config, DevClassForName loader) {
		this.routeLoader = routeConfig;
		this.config = config;
		this.classLoader = loader;
		this.lastFileTimestamp = config.getMetaFile().lastModified();
	}

	@Override
	public void start() {
		if(config.getOverridesModule() != null) {
			throw new IllegalArgumentException("Any development server cannot be passed overrides through the api."
				+ "  ie. config.getOverridesModule must be null.  If you want to override in a DevServer, then "
				+ "look at some of the development test examples and you need to override CompileOnDemand as well");
		}
		
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
		if(meta.getControllerInstance() == null) {
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
		public MatchResult fetchNotfoundRoute(NotFoundException e) {
			return fetchNotFoundRoute(e, req);
		}

		@Override
		public MatchResult fetchInternalServerErrorRoute() {
			return fetchInternalErrorRoute(req);
		}
		
	}
	
	public MatchResult fetchNotFoundRoute(NotFoundException e, RouterRequest req) {
		log.error("(Development only log message) Route not found!!! Either you(developer) typed the wrong url OR you have a bad route.  Either way,\n"
				+ " something needs a'fixin.  req="+req, e);
		
		MatchResult result = routeLoader.fetchNotFoundRoute();
		
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			routeLoader.loadControllerIntoMetaObject(meta, false);
		}
		
		return result;
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
