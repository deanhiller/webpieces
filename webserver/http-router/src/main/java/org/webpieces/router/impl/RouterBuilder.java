package org.webpieces.router.impl;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.Injector;

public class RouterBuilder implements Router {

	private static final Logger log = LoggerFactory.getLogger(RouterBuilder.class);
	
	public static ThreadLocal<RouteModuleInfo> currentPackage = new ThreadLocal<>();
	public static ThreadLocal<Injector> injector = new ThreadLocal<>();
	
	private final AllRoutingInfo info;
	private ReverseRoutes reverseRoutes;
	private List<StaticRoute> staticRoutes = new ArrayList<>();
	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();
	private ControllerLoader finder;

	private String routerPath;

	private Charset urlEncoding;

	private int staticRouteIdCounter;

	public RouterBuilder(String path, AllRoutingInfo info, ReverseRoutes reverseRoutes, ControllerLoader finder, Charset urlEncoding) {
		this.routerPath = path;
		this.info = info;
		this.reverseRoutes = reverseRoutes;
		this.finder = finder;
		this.urlEncoding = urlEncoding;
	}
	
	public void addRoute(Route r, RouteId routeId) {
		log.info("scope:'"+routerPath+"' adding route="+r.getFullPath()+" method="+r.getControllerMethodString());
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
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(method, p, controllerMethod, routeId, false, checkSecureToken);
		addRoute(route, routeId);
	}

	@Override
	public void addRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId, boolean checkToken) {
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(method, p, controllerMethod, routeId, false, checkToken);
		addRoute(route, routeId);
	}
	
	@Override
	public void addRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(methods, p, controllerMethod, routeId, false, false);
		addRoute(route, routeId);
	}

	@Override
	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true; //do this by default (later, add methods to avoid secureToken check
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(method, p, controllerMethod, routeId, true, checkSecureToken);
		addRoute(route, routeId);
	}

	@Override
	public void addHttpsRoute(HttpMethod method, String path, String controllerMethod, RouteId routeId,
			boolean checkToken) {
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(method, p, controllerMethod, routeId, true, checkToken);
		addRoute(route, routeId);
	}
	
	@Override
	public void addHttpsRoute(Set<HttpMethod> methods, String path, String controllerMethod, RouteId routeId) {
		UrlPath p = new UrlPath(routerPath, path);
		Route route = new RouteImpl(methods, p, controllerMethod, routeId, true, false);
		addRoute(route, routeId);
	}

	@Override
	public void addStaticDir(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(!urlPath.endsWith("/"))
			throw new IllegalArgumentException("Static directory so urlPath must end with a /");
		addStaticRoute(urlPath, fileSystemPath, isOnClassPath);
	}

	@Override
	public void addStaticFile(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(urlPath.endsWith("/"))
			throw new IllegalArgumentException("Static file so urlPath must NOT end with a /");

		addStaticRoute(urlPath, fileSystemPath, isOnClassPath);
	}

	private void addStaticRoute(String urlPath, String fileSystemPath, boolean isOnClassPath) {
		if(isOnClassPath)
			throw new UnsupportedOperationException("oops, isOnClassPath not supported yet");
		
		StaticRoute route = new StaticRoute(getUniqueId(), new UrlPath(routerPath, urlPath), fileSystemPath, isOnClassPath);
		staticRoutes.add(route);
		log.info("scope:'"+routerPath+"' adding static route="+route.getFullPath()+" fileSystemPath="+route.getFileSystemPath());
		RouteMeta meta = new RouteMeta(route, injector.get(), currentPackage.get(), urlEncoding);
		info.addRoute(meta);
	}
	
	private synchronized int getUniqueId() {
		return staticRouteIdCounter++;
	}
	
	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		String totalPath = routerPath+path;
		FilterInfo<T> info = new FilterInfo<>(totalPath, filter, initialConfig, type);
		routeFilters.add(info);
	}
	
	@Override
	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type);
		notFoundFilters.add(info);
	}

	@Override
	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type);
		internalErrorFilters.add(info);
	}
	
	@Override
	public Router getScopedRouter(String path) {
		if(path == null || path.length() == 0)
			throw new IllegalArgumentException("path must be non-null and length must be greater than 0");
		AllRoutingInfo subInfo = info.addScope(path);
		return new RouterBuilder(this.routerPath+path, subInfo, reverseRoutes, finder, urlEncoding);
	}

	/*
	 * Adds routes like the following all in one method
	 * 
	 * 	//addRoute(GET ,   "/user/list",        "crud/CrudUserController.userList", listRoute);
	 *	//addRoute(GET ,   "/user/new",         "crud/CrudUserController.userAddEdit", addRoute);
	 *	//addRoute(GET ,   "/user/edit/{id}",   "crud/CrudUserController.userAddEdit", editRoute);
	 *	//addRoute(POST,   "/user/post",        "crud/CrudUserController.postSaveUser", saveRoute);
	 *	//addRoute(GET,    "/user/delete/{id}", "crud/CrudUserController.postDeleteUser", deleteRoute);
	 */
	@Override
	public void addCrud(String entity, String controller,  
			RouteId listRoute, RouteId addRoute, RouteId editRoute, RouteId saveRoute, RouteId deleteRoute) {
		String entityWithCapital = entity.substring(0, 1).toUpperCase() + entity.substring(1);
		addRoute(GET , "/"+entity+"/list",        controller+"."+entity+"List", listRoute);
		addRoute(GET , "/"+entity+"/new",         controller+"."+entity+"AddEdit", addRoute);
		addRoute(GET , "/"+entity+"/edit/{id}",   controller+"."+entity+"AddEdit", editRoute);
		addRoute(POST, "/"+entity+"/post",        controller+".postSave"+entityWithCapital, saveRoute);
		
		//NOTE: Browsers don't support DELETE.  POST might make more sense here for delete but GET is way way less html
		//code(ok, 1 line instead of 3).  There are hacks with javascript to support DELETE but seriously, we should just
		//KISS and YAGNI (google that if you don't know).  
		//HOWEVER, If you don't like this, copy and paste this method and modify to be a POST OR DELETE and add the 
		//javascript for next time
		addRoute(POST, "/"+entity+"/delete/{id}", controller+".postDelete"+entityWithCapital, deleteRoute);
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
		log.info("scope:'"+routerPath+"' adding PAGE_NOT_FOUND route="+r.getFullPath()+" method="+r.getControllerMethodString());
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
		log.info("scope:'"+routerPath+"' adding INTERNAL_SVR_ERROR route="+r.getFullPath()+" method="+r.getControllerMethodString());
		RouteMeta meta = new RouteMeta(r, injector.get(), currentPackage.get(), urlEncoding);
		finder.loadControllerIntoMetaObject(meta, true);
		info.setInternalSvrErrorRoute(meta);
	}

	public List<StaticRoute> getStaticRoutes() {
		return staticRoutes;
	}

	public void applyFilters() {
		Collection<RouteMeta> metas = reverseRoutes.getAllRouteMetas();
		for(RouteMeta meta : metas) {
			String path = meta.getRoute().getFullPath();
			List<FilterInfo<?>> filters = findMatchingFilters(path, meta.getRoute().isHttpsRoute());
			meta.setFilters(filters);
		}
		
		RouteMeta errorMeta = this.info.getInternalErrorRoute();
		errorMeta.setFilters(internalErrorFilters);
		
		RouteMeta notFoundMeta = this.info.getPageNotfoundRoute();
		notFoundMeta.setFilters(notFoundFilters);
	}

	public List<FilterInfo<?>> findMatchingFilters(String path, boolean isHttps) {
		List<FilterInfo<?>> matchingFilters = new ArrayList<>();
		for(FilterInfo<?> info : routeFilters) {
			if(!info.securityMatch(isHttps))
				continue; //skip this filter
			
			Pattern patternToMatch = info.getPatternToMatch();
			Matcher matcher = patternToMatch.matcher(path);
			if(matcher.matches()) {
				matchingFilters.add(info);
			}
		}
		return matchingFilters;
	}

	public RouteMeta getNotFoundMeta() {
		return info.getPageNotfoundRoute();
	}

	public RouteMeta getInternalErrorMeta() {
		return info.getInternalErrorRoute();
	}

}
