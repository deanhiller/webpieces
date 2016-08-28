package org.webpieces.router.impl;

import java.nio.charset.Charset;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.HttpFilter;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.loader.ControllerLoader;

import com.google.inject.Injector;

public class RouterBuilder implements Router {

	private static final Logger log = LoggerFactory.getLogger(RouterBuilder.class);
	
	public static ThreadLocal<RouteModuleInfo> currentPackage = new ThreadLocal<>();
	public static ThreadLocal<Injector> injector = new ThreadLocal<>();
	
	private final AllRoutingInfo info;
	private ReverseRoutes reverseRoutes;
	private ControllerLoader finder;

	private String routerPath;

	private Charset urlEncoding;

	public RouterBuilder(String path, AllRoutingInfo info, ReverseRoutes reverseRoutes, ControllerLoader finder, Charset urlEncoding) {
		this.routerPath = path;
		this.info = info;
		this.reverseRoutes = reverseRoutes;
		this.finder = finder;
		this.urlEncoding = urlEncoding;
	}
	
	public void addRoute(Route r, RouteId routeId) {
		log.info("scope:'"+routerPath+"' adding route="+r.getPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), urlEncoding);
		finder.loadControllerIntoMetaObject(meta, true);

		info.addRoute(meta);
		
		reverseRoutes.addRoute(routeId, meta);
	}

	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true; //do this by default (later, add methods to avoid secureToken check
		Route route = new RouteImpl(method, path, controllerMethod, routeId, false, checkSecureToken);
		addRoute(route, routeId);
	}

	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, false, checkToken);
		addRoute(route, routeId);		
	}
	
	@Override
	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, false, false);
		addRoute(route, routeId);
	}

	@Override
	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true; //do this by default (later, add methods to avoid secureToken check
		Route route = new RouteImpl(method, path, controllerMethod, routeId, true, checkSecureToken);
		addRoute(route, routeId);
	}

	@Override
	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId,
			boolean checkToken) {
		Route route = new RouteImpl(method, path, controllerMethod, routeId, true, checkToken);
		addRoute(route, routeId);
	}
	
	@Override
	public void addHttpsRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		Route route = new RouteImpl(methods, path, controllerMethod, routeId, true, false);
		addRoute(route, routeId);
	}

	@Override
	public void addStaticDir(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(!urlPath.endsWith("/"))
			throw new IllegalArgumentException("Static directory so urlPath must end with a /");
		else if(!fileSystemPath.endsWith("/"))
			throw new IllegalArgumentException("Static directory so fileSystemPath must end with a /");
		addStaticRoute(urlPath, fileSystemPath, isOnClassPath);
	}

	@Override
	public void addStaticFile(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(urlPath.endsWith("/"))
			throw new IllegalArgumentException("Static file so urlPath must NOT end with a /");
		else if(fileSystemPath.endsWith("/"))
			throw new IllegalArgumentException("Static file so fileSystemPath must NOT end with a /");
		addStaticRoute(urlPath, fileSystemPath, isOnClassPath);
	}

	private void addStaticRoute(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(isOnClassPath)
			throw new UnsupportedOperationException("oops, isOnClassPath not supported yet");
		
		Route route = new StaticRoute(urlPath, fileSystemPath, isOnClassPath);
		RouteMeta meta = new RouteMeta(route, injector.get(), currentPackage.get(), urlEncoding);
		info.addRoute(meta);
	}
	
	@Override
	public void addFilter(String path, HttpFilter securityFilter) {
	}

	@Override
	public Router getScopedRouter(String path, boolean isSecure) {
		if(path == null || path.length() == 0)
			throw new IllegalArgumentException("path must be non-null and length must be greater than 0");
		AllRoutingInfo subInfo = info.addScope(path);
		return new RouterBuilder(path, subInfo, reverseRoutes, finder, urlEncoding);
	}

	public AllRoutingInfo getRouterInfo() {
		return info;
	}

	public ReverseRoutes getReverseRoutes() {
		return reverseRoutes; 
	}
	
	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod, RouteType.NOT_FOUND);
		setNotFoundRoute(route);
	}

	public void setNotFoundRoute(Route r) {
		if(!"".equals(this.routerPath))
			throw new UnsupportedOperationException("setNotFoundRoute can only be called on the root Router, not a scoped router");
		log.info("scope:'"+routerPath+"' adding PAGE_NOT_FOUND route="+r.getPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), urlEncoding);
		finder.loadControllerIntoMetaObject(meta, true);
		info.setPageNotFoundRoute(meta);
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		Route route = new RouteImpl(controllerMethod, RouteType.INTERNAL_SERVER_ERROR);
		setInternalSvrErrorRoute(route);
	}
	
	public void setInternalSvrErrorRoute(Route r) {
		if(!"".equals(this.routerPath))
			throw new UnsupportedOperationException("setInternalSvrErrorRoute can only be called on the root Router, not a scoped router");
		log.info("scope:'"+routerPath+"' adding INTERNAL_SVR_ERROR route="+r.getPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), urlEncoding);
		finder.loadControllerIntoMetaObject(meta, true);
		info.setInternalSvrErrorRoute(meta);
	}

}
