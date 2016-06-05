package org.webpieces.router.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouteModule;
import org.webpieces.router.api.RouterModules;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.loader.Loader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public abstract class RouterConfig {
	private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);
	
	private RouteInfo routeInfo;
	private ReverseRoutes reverseRoutes;
	private VirtualFile modules;
	private Module overrideModule;
	protected Loader loader;
	protected RouterImpl router;

	protected RouterModules routerModule;
	
	public RouterConfig(VirtualFile modules, Module overrideModule, Loader loader) {
		this.modules = modules;
		this.overrideModule = overrideModule;
		this.loader = loader;
	}
	
	public void load() {
		try {
			loadImpl(modules);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void loadImpl(VirtualFile modules) throws IOException {
		log.info("loading the master "+RouterModules.class.getSimpleName()+" class file");

		String moduleName;
		try (InputStream str = modules.openInputStream()) {
			InputStreamReader reader = new InputStreamReader(str);
			BufferedReader bufReader = new BufferedReader(reader);
			moduleName = bufReader.readLine().trim();
		}

		log.info(RouterModules.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = loader.clazzForName(moduleName);
		Object obj = newInstance(clazz);
		if(!(obj instanceof RouterModules))
			throw new IllegalArgumentException("name="+moduleName+" does not implement "+RouterModules.class.getSimpleName());

		log.info(RouterModules.class.getSimpleName()+" loaded");

		routerModule = (RouterModules) obj;
		Injector injector = createInjector(routerModule);

		addRoutes(routerModule, injector);
	}

	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void addRoutes(RouterModules rm, Injector injector) {
		log.info("adding routes");
		
		routeInfo = new RouteInfo();
		reverseRoutes = new ReverseRoutes();
		
		router = new RouterImpl(routeInfo, reverseRoutes, loader, injector);
		
		for(RouteModule module : rm.getRouterModules()) {
			String packageName = module.getClass().getPackage().getName();
			RouterImpl.currentPackage = packageName;
			module.configure(router, packageName);
		}
		
		if(!routeInfo.isCatchallRouteSet())
			throw new IllegalStateException("Client RouterModule did not call top level router.setCatchAllRoute");
		
		log.info("added all routes to router");
	}
	
	private Injector createInjector(RouterModules rm) {
		List<Module> guiceModules = rm.getGuiceModules();
		
		Module module = Modules.combine(guiceModules);
		if(overrideModule != null)
			module = Modules.override(module).with(overrideModule);
		
		Injector injector = Guice.createInjector(module);
		return injector;
	}
	
	private Object newInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created(are you missing default constructor? is it not public?)", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Your clazz="+clazz.getSimpleName()+" could not be created", e);
		}
	}

	/**
	 * Only used with DevRouterConfig which is not on classpath in prod mode
	 * 
	 * @return
	 */
	public abstract boolean reloadIfTextFileChanged();

	public abstract void processHttpRequests(Request req);
	
	public RouteMeta fetchRoute(Request req) {
		RouteMeta meta = routeInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	protected void invokeRoute(RouteMeta meta, Request req) {
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		invokeMethod(obj, method);
		
	}
	
	private Object invokeMethod(Object obj, Method m) {
		try {
			return m.invoke(obj, (Object[])null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
