package org.webpieces.router.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.hooks.ClassForName;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouteLoader {
	private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);
	
	private final RouterConfig config;
	private final RouteInvoker invoker;
	private final ControllerLoader controllerFinder;

	protected RouterBuilder routerBuilder;
	
	@Inject
	public RouteLoader(RouterConfig config, 
						RouteInvoker invoker,
						ControllerLoader controllerFinder
	) {
		this.config = config;
		this.invoker = invoker;
		this.controllerFinder = controllerFinder;
	}
	
	public WebAppMeta load(ClassForName loader) {
		try {
			return loadImpl(loader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private WebAppMeta loadImpl(ClassForName loader) throws IOException {
		log.info("loading the master "+WebAppMeta.class.getSimpleName()+" class file");

		VirtualFile fileWithMetaClassName = config.getMetaFile();
		String moduleName;
		
		Charset fileEncoding = config.getFileEncoding();
		String contents = fileWithMetaClassName.contentAsString(fileEncoding);
		moduleName = contents.trim();
		
		log.info(WebAppMeta.class.getSimpleName()+" class to load="+moduleName);
		Class<?> clazz = loader.clazzForName(moduleName);
		Object obj = newInstance(clazz);
		if(!(obj instanceof WebAppMeta))
			throw new IllegalArgumentException("name="+moduleName+" does not implement "+WebAppMeta.class.getSimpleName());

		log.info(WebAppMeta.class.getSimpleName()+" loaded");

		WebAppMeta routerModule = (WebAppMeta) obj;
		Injector injector = createInjector(routerModule);

		loadAllRoutes(routerModule, injector);
		return routerModule;
	}

	public Injector createInjector(WebAppMeta routerModule) {
		List<Module> guiceModules = routerModule.getGuiceModules();
		
		Module module = Modules.combine(guiceModules);
		if(config.getOverridesModule() != null)
			module = Modules.override(module).with(config.getOverridesModule());
		
		Injector injector = Guice.createInjector(module);
		return injector;
	}
	
	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void loadAllRoutes(WebAppMeta rm, Injector injector) {
		log.info("adding routes");
		
		ReverseRoutes reverseRoutes = new ReverseRoutes(config.getUrlEncoding());
		routerBuilder = new RouterBuilder("", new AllRoutingInfo(), reverseRoutes, controllerFinder, config.getUrlEncoding());
		invoker.init(reverseRoutes);
		
		for(RouteModule module : rm.getRouteModules()) {
			Class<?> clazz = module.getClass();
			String packageName = getPackage(clazz);
			RouterBuilder.currentPackage.set(packageName);
			RouterBuilder.injector.set(injector);
			module.configure(routerBuilder, packageName);
			RouterBuilder.currentPackage.set(null);
			RouterBuilder.injector.set(null);
		}
		
		reverseRoutes.finalSetup();
		
		if(!routerBuilder.getRouterInfo().isPageNotFoundRouteSet())
			throw new IllegalStateException("None of the RouteModule implementations called top level router.setNotFoundRoute.  Modules="+rm.getRouteModules());
		else if(!routerBuilder.getRouterInfo().isInternalSvrErrorRouteSet())
			throw new IllegalStateException("None of the RouteModule implementations called top level router.setInternalSvrErrorRoute.  Modules="+rm.getRouteModules());
			
		
		log.info("added all routes to router");
	}
	
	private String getPackage(Class<?> clazz) {
		int lastIndexOf = clazz.getName().lastIndexOf(".");
		String pkgName = clazz.getName().substring(0, lastIndexOf);
		return pkgName;
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

	public MatchResult fetchRoute(RouterRequest req) {
		AllRoutingInfo allRoutingInfo = routerBuilder.getRouterInfo();
		MatchResult meta = allRoutingInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public void invokeRoute(MatchResult result, RouterRequest req, ResponseStreamer responseCb, ErrorRoutes notFoundRoute) {
		//This class is purely the RouteLoader so delegate and encapsulate the invocation in RouteInvoker....
		invoker.invoke(result, req, responseCb, notFoundRoute);
	}

	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		controllerFinder.loadControllerIntoMetaObject(meta, isInitializingAllControllers);
	}

	public MatchResult fetchNotFoundRoute() {
		AllRoutingInfo routerInfo = routerBuilder.getRouterInfo();
		RouteMeta notfoundRoute = routerInfo.getPageNotfoundRoute();
		return new MatchResult(notfoundRoute);
	}

	public MatchResult fetchInternalErrorRoute() {
		AllRoutingInfo routerInfo = routerBuilder.getRouterInfo();
		RouteMeta internalErrorRoute = routerInfo.getInternalErrorRoute();
		return new MatchResult(internalErrorRoute);
	}

	public String convertToUrl(String routeId, Map<String, String> args) {
		return invoker.convertToUrl(routeId, args);
	}
	
}
