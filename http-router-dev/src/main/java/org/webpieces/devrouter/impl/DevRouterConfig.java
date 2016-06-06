package org.webpieces.devrouter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.RouterConfig;

public class DevRouterConfig extends RouterConfig {

	private static final Logger log = LoggerFactory.getLogger(DevRouterConfig.class);
	private long lastFileTimestamp;

	public DevRouterConfig(HttpRouterConfig config, DevLoader loader) {
		super(config, loader);
		this.lastFileTimestamp = routerModulesTextFile.lastModified();
	}

	@Override
	public void processHttpRequests(Request req) {
		//In DevRouter, check if we need to reload the text file as it points to a new RouterModules.java implementation file
		boolean reloaded = reloadIfTextFileChanged();
		
		if(!reloaded)
			reloadIfClassFilesChanged();
		
		RouteMeta meta = fetchRoute(req);
		
		if(meta.getControllerInstance() == null) {
			router.loadControllerIntoMetaObject(meta, false);
		}
		
		invokeRoute(meta, req);
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

		load();
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
