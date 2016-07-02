package org.webpieces.router.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpRouterConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.loader.ClassForName;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class RouteLoader {
	private static final Logger log = LoggerFactory.getLogger(RouteLoader.class);
	
	private HttpRouterConfig config;
	protected RouterBuilder routerBuilder;
	private RouteInvoker invoker;

	private ControllerLoader controllerFinder;

	@Inject
	public RouteLoader(HttpRouterConfig config, 
						RouteInvoker invoker,
						ControllerLoader controllerFinder) {
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
		try (InputStream str = fileWithMetaClassName.openInputStream()) {
			InputStreamReader reader = new InputStreamReader(str);
			BufferedReader bufReader = new BufferedReader(reader);
			moduleName = bufReader.readLine().trim();
		}

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

	//protected abstract void verifyRoutes(Collection<Route> allRoutes);

	public void loadAllRoutes(WebAppMeta rm, Injector injector) {
		log.info("adding routes");
		
		routerBuilder = new RouterBuilder("", new RouteInfo(), new ReverseRoutes(), controllerFinder);
		
		for(RouteModule module : rm.getRouteModules()) {
			Class<?> clazz = module.getClass();
			String packageName = getPackage(clazz);
			RouterBuilder.currentPackage.set(packageName);
			RouterBuilder.injector.set(injector);
			module.configure(routerBuilder, packageName);
			RouterBuilder.currentPackage.set(null);
			RouterBuilder.injector.set(null);
		}
		
		if(!routerBuilder.getRouterInfo().isPageNotFoundRouteSet())
			throw new IllegalStateException("None of the RouteModule implementations called top level router.setNotFoundRoute.  Modules="+rm.getRouteModules());
		
		log.info("added all routes to router");
	}
	
	private String getPackage(Class<?> clazz) {
		int lastIndexOf = clazz.getName().lastIndexOf(".");
		String pkgName = clazz.getName().substring(0, lastIndexOf);
		return pkgName;
	}

	private Injector createInjector(WebAppMeta rm) {
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

	public MatchResult fetchRoute(RouterRequest req) {
		RouteInfo routerInfo = routerBuilder.getRouterInfo();
		MatchResult meta = routerInfo.fetchRoute(req, req.relativePath);
		if(meta == null)
			throw new IllegalStateException("missing exception on creation if we go this far");

		return meta;
	}
	
	public void invokeRoute(MatchResult result, RouterRequest req, ResponseStreamer responseCb, Supplier<MatchResult> notFoundRoute) {
		try {
			//This makes us consistent with other NotFoundExceptions
			if(result.getMeta().getRoute().isNotFoundRoute())
				notFound(null, "No route found for path="+req.relativePath, notFoundRoute, req, responseCb);

			invoker.invoke(routerBuilder.getReverseRoutes(), result, req, responseCb);
		} catch(NotFoundException e) {
			notFound(e, e.getMessage(), notFoundRoute, req, responseCb);
		}
	}

	private void notFound(NotFoundException e, String message, Supplier<MatchResult> notFoundRoute, RouterRequest req, ResponseStreamer responseCb) {
		MatchResult result = notFoundRoute.get();
		invoker.invoke(routerBuilder.getReverseRoutes(), result, req, responseCb);
	}

	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		controllerFinder.loadControllerIntoMetaObject(meta, isInitializingAllControllers);
	}

	public MatchResult fetchNotFoundRoute() {
		RouteInfo routerInfo = routerBuilder.getRouterInfo();
		RouteMeta notfoundRoute = routerInfo.getPageNotfoundRoute();
		return new MatchResult(notfoundRoute);
	}
}
