package org.webpieces.router.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.Route;
import org.webpieces.router.api.RouteId;
import org.webpieces.router.api.Router;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.impl.loader.Loader;

import com.google.inject.Injector;

public class RouterImpl implements Router {

	public static String currentPackage;
	
	private final RouteInfo info;
	private ReverseRoutes reverseRoutes;
	private Injector injector;
	private Loader loader;
	
	public RouterImpl(RouteInfo info, ReverseRoutes reverseRoutes, Loader loader, Injector injector) {
		this.info = info;
		this.reverseRoutes = reverseRoutes;
		this.injector = injector;
		this.loader = loader;
	}
	
	@Override
	public void addRoute(Route r, RouteId routeId) {		
		RouteMeta meta = new RouteMeta(r);
		loadControllerIntoMetaObject(meta, true);
		
		info.addRoute(meta);
		reverseRoutes.addRoute(routeId, meta);
	}
	
	/**
	 * isInitializingAllControllers is true if in process of initializing ALL controllers and false if just being called to
	 * initialize on controller
	 * 
	 * @param meta
	 * @param isInitializingAllControllers
	 */
	public void loadControllerIntoMetaObject(RouteMeta meta, boolean isInitializingAllControllers) {
		Route r = meta.getRoute();
		String controllerAndMethod = r.getControllerMethodString();
		int lastIndex = controllerAndMethod.lastIndexOf(".");
		int fromBeginIndex = controllerAndMethod.indexOf(".");
		String methodStr = controllerAndMethod.substring(lastIndex+1);
		String controllerStr = controllerAndMethod.substring(0, lastIndex);
		if(lastIndex == fromBeginIndex) {
			controllerStr = currentPackage+"."+controllerStr;
		}
		
		Object controllerInst = loader.createController(injector, controllerStr, isInitializingAllControllers);
		if(controllerInst == null) {
			//delayed construction for dev mode only(I hate having this code here as it should not be part of the prod server!!!)
			//at least the DevLoader is only in the dev server
			//TODO: Move this into the Loader I think....
			return;
		}
		
		Method[] methods = controllerInst.getClass().getMethods();
		List<Method> matches = new ArrayList<>();
		for(Method m : methods) {
			if(m.getName().equals(methodStr))
				matches.add(m);
		}

		if(matches.size() == 0)
			throw new IllegalArgumentException("Invalid Route.  Cannot find 'public' method="+methodStr+" on class="+controllerStr);
		else if(matches.size() > 1) 
			throw new UnsupportedOperationException("You have more than one 'public' method named="+methodStr+" on class="+controllerStr+"  This is not yet supported until we support method parameters");
		
		Method method = matches.get(0);
		meta.setControllerInstance(controllerInst);
		meta.setMethod(method);
	}

	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, false);
		addRoute(route, routeId);
	}

	@Override
	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, false);
		addRoute(route, routeId);
	}

	@Override
	public void addSecureRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, true);
		addRoute(route, routeId);
	}

	@Override
	public void addSecureRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, true);
		addRoute(route, routeId);
	}

	@Override
	public void addStaticGetRoute(String path, File f) {
	}

	@Override
	public void addFilter(String path, HttpFilter securityFilter) {
	}

	@Override
	public Router getScopedRouter(String path, boolean isSecure) {
		RouteInfo subInfo = info.addScope(path);
		return new RouterImpl(subInfo, reverseRoutes, loader, injector);
	}

	public RouteInfo getRouterInfo() {
		return info;
	}

	@Override
	public void setCatchAllRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod);
		setCatchAllRoute(route);
	}

	@Override
	public void setCatchAllRoute(Route r) {
		RouteMeta meta = new RouteMeta(r);
		loadControllerIntoMetaObject(meta, true);	
		info.setCatchAllRoute(meta);
	}
	
}
