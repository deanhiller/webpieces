package org.webpieces.router.impl;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.loader.ControllerLoader;

import com.google.inject.Injector;

public class RouterBuilder implements Router {

	private static final Logger log = LoggerFactory.getLogger(RouterBuilder.class);
	
	public static ThreadLocal<String> currentPackage = new ThreadLocal<>();
	public static ThreadLocal<Injector> injector = new ThreadLocal<>();
	
	private final RouteInfo info;
	private ReverseRoutes reverseRoutes;
	private ControllerLoader finder;

	private String routerPath;

	public RouterBuilder(String path, RouteInfo info, ReverseRoutes reverseRoutes, ControllerLoader finder) {
		this.routerPath = path;
		this.info = info;
		this.reverseRoutes = reverseRoutes;
		this.finder = finder;
	}
	
	@Override
	public void addPostRoute(String path, String controllerMethod) {
		Route route = new RouteImpl(HttpMethod.POST, path, controllerMethod, null, false);
		addRoute(route, null);
	}
	
	public void addRoute(Route r, RouteId routeId) {
		log.info("scope:'"+routerPath+"' adding route="+r.getPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), false);
		finder.loadControllerIntoMetaObject(meta, true);

		info.addRoute(meta);
		
		//POST methods have no route Id as we only redirect to GET pages
		if(routeId != null)
			reverseRoutes.addRoute(routeId, meta);
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
		if(path == null || path.length() == 0)
			throw new IllegalArgumentException("path must be non-null and length must be greater than 0");
		RouteInfo subInfo = info.addScope(path);
		return new RouterBuilder(path, subInfo, reverseRoutes, finder);
	}

	public RouteInfo getRouterInfo() {
		return info;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes; 
	}
	
	@Override
	public void setNotFoundRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod);
		setNotFoundRoute(route);
	}

	public void setNotFoundRoute(Route r) {
		if(!"".equals(this.routerPath))
			throw new UnsupportedOperationException("setNotFoundRoute can only be called on the root Router, not a scoped router");
		log.info("scope:'"+routerPath+"' adding PAGE_NOT_FOUND route="+r.getPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), true);
		finder.loadControllerIntoMetaObject(meta, true);
		info.setPageNotFoundRoute(meta);
	}

}
