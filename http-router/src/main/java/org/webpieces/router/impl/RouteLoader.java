package org.webpieces.router.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.dto.Request;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.RouterModules;
import org.webpieces.router.impl.loader.Loader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouteLoader {
	private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);
	
	private HttpRouterConfig config;
	private RouteInfo routeInfo;
	private ReverseRoutes reverseRoutes;
	
	protected RouterImpl router;

	@Inject
	public RouteLoader(HttpRouterConfig config) {
		this.config = config;
	}
	
	public RouterModules load(Loader loader) {
		try {
			return loadImpl(loader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private RouterModules loadImpl(Loader loader) throws IOException {
		log.info("loading the master "+RouterModules.class.getSimpleName()+" class file");

		VirtualFile fileWithRouterModulesName = config.getRoutersFile();
		String moduleName;
		try (InputStream str = fileWithRouterModulesName.openInputStream()) {
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

		RouterModules routerModule = (RouterModules) obj;
		Injector injector = createInjector(routerModule);

		addRoutes(routerModule, injector, loader);
		return routerModule;
	}

	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void addRoutes(RouterModules rm, Injector injector, Loader loader) {
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
		if(config.getOverridesModule() != null)
			module = Modules.override(module).with(config.getOverridesModule());
		
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

	public RouteMeta fetchRoute(Request req) {
		RouteMeta meta = routeInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public void invokeRoute(RouteMeta meta, Request req) {
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

	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		router.loadControllerIntoMetaObject(meta, isInitializingAllControllers);
	}
}
