package org.webpieces.router.impl;

import java.io.File;
import java.util.Set;

import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.loader.Loader;

import com.google.inject.Injector;

public class RouterBuilder implements Router {

	public static String currentPackage;
	
	private final RouteInfo info;
	private ReverseRoutes reverseRoutes;
	private Injector injector;
	private Loader loader;
	
	public RouterBuilder(RouteInfo info, ReverseRoutes reverseRoutes, Loader loader, Injector injector) {
		this.info = info;
		this.reverseRoutes = reverseRoutes;
		this.injector = injector;
		this.loader = loader;
	}
	
	@Override
	public void addPostRoute(String path, String controllerMethod) {
		Route route = new RouteImpl(HttpMethod.POST, path, controllerMethod, null, false);
		addRoute(route, null);
	}
	
	public void addRoute(Route r, RouteId routeId) {
		RouteMeta meta = new RouteMeta(r);
		loadControllerIntoMetaObject(meta, true);

		info.addRoute(meta);
		
		//POST methods have no route Id as we only redirect to GET pages
		if(routeId != null)
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
		
		loader.loadControllerIntoMeta(meta, injector, controllerStr, methodStr, isInitializingAllControllers);
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
	public void addSecurePostRoute(String path, String controllerMethod) {
		Route route = new RouteImpl(HttpMethod.POST, path, controllerMethod, null, true);
		addRoute(route, null);
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
		return new RouterBuilder(subInfo, reverseRoutes, loader, injector);
	}

	public RouteInfo getRouterInfo() {
		return info;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes; 
	}
	
	@Override
	public void setCatchAllRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod);
		setCatchAllRoute(route);
	}

	public void setCatchAllRoute(Route r) {
		RouteMeta meta = new RouteMeta(r);
		loadControllerIntoMetaObject(meta, true);	
		info.setCatchAllRoute(meta);
	}
	
}
