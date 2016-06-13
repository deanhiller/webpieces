package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.routing.RouterModules;
import org.webpieces.router.impl.MatchResult;
import org.webpieces.router.impl.RouteLoader;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.util.file.VirtualFile;

public class DevRoutingService implements RoutingService {

	private static final Logger log = LoggerFactory.getLogger(DevRoutingService.class);
	private long lastFileTimestamp;
	private RouteLoader routeConfig;
	private DevLoader loader;
	private VirtualFile routerModulesTextFile;
	private RouterModules routerModule;

	@Inject
	public DevRoutingService(RouteLoader routeConfig, HttpRouterConfig config, DevLoader loader) {
		routerModulesTextFile = config.getRoutersFile();
		this.routeConfig = routeConfig;
		this.loader = loader;
		this.lastFileTimestamp = routerModulesTextFile.lastModified();
	}

	@Override
	public void start() {
		log.info("Starting DEVELOPMENT server with CompilingClassLoader and HotSwap");
		load();
	}

	@Override
	public void stop() {
	}
	
	private void load() {
		routerModule = routeConfig.load(loader);		
	}
	
	@Override
	public void processHttpRequests(Request req, ResponseStreamer responseCb) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		MatchResult result = routeConfig.fetchRoute(req);
		
		RouteMeta meta = result.getMeta();
		if(meta.getControllerInstance() == null) {
			routeConfig.loadControllerIntoMetaObject(meta, false);
		}
		
		routeConfig.invokeRoute(result, req, responseCb);
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

		routerModule = routeConfig.load(loader);
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
		load();
	}

}
