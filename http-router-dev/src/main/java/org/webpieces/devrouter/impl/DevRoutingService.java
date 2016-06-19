package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.routing.WebAppMetaInfo;
import org.webpieces.router.impl.AbstractRouterService;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.util.file.VirtualFile;

public class DevRoutingService extends AbstractRouterService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private long lastFileTimestamp;
	private RouteLoader routeLoader;
	private DevLoader loader;
	private VirtualFile routerModulesTextFile;
	private WebAppMetaInfo routerModule;

	@Inject
	public DevRoutingService(RouteLoader routeConfig, HttpRouterConfig config, DevLoader loader) {
		routerModulesTextFile = config.getRoutersFile();
		this.routeLoader = routeConfig;
		this.loader = loader;
		this.lastFileTimestamp = routerModulesTextFile.lastModified();
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
	public void processHttpRequestsImpl(Request req, ResponseStreamer responseCb) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		MatchResult result = routeLoader.fetchRoute(req);
		
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			routeLoader.loadControllerIntoMetaObject(meta, false);
		}
		
		routeLoader.invokeRoute(result, req, responseCb, () -> fetchNotFoundRoute());
	}
	
	public MatchResult fetchNotFoundRoute() {
		MatchResult result = routeLoader.fetchNotFoundRoute();
		
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
		//if timestamp the same, no changes
		if(lastFileTimestamp == routerModulesTextFile.lastModified())
			return false;

		log.info("text file changed so need to reload RouterModules.java implementation");

		routerModule = routeLoader.load(loader);
		lastFileTimestamp = routerModulesTextFile.lastModified();
		return true;
	}

	private void reloadIfClassFilesChanged() {
		String routerModuleClassName = routerModule.getClass().getName();
		ClassLoader previousCl = routerModule.getClass().getClassLoader();
		
		Class<?> newClazz = loader.clazzForName(routerModuleClassName);
		ClassLoader newClassLoader = newClazz.getClassLoader();
		if(previousCl == newClassLoader)
			return;
		
		log.info("classloader change so we need to reload all router classes");
		loadOrReload();
	}

	private void loadOrReload() {
		routerModule = routeLoader.load(loader);		
	}
}
