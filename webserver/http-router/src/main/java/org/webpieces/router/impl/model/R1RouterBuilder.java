package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class R1RouterBuilder extends AbstractDomainBuilder implements Router  {

	private static final Logger log = LoggerFactory.getLogger(R2DomainRouterBuilder.class);

	private L1AllRouting allRouting;

	private List<StaticRoute> staticRoutes = new ArrayList<>();
	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();
	
	/**
	 * This is really bad!!!  static means two webservers use the same values..ick!!!
	 */
	private static int staticRouteIdCounter;
	
	public R1RouterBuilder(RouterInfo info, L1AllRouting allRouting, LogicHolder holder) {
		super(info, allRouting.getMainRoutes(), allRouting.getMainRoutes().getRoutesForDomain(), holder);
		this.allRouting = allRouting;
	}

	@Override
	public Router getDomainScopedRouter(String domain) {
		if(domain == null || domain.length() == 0)
			throw new IllegalArgumentException("domain must be non-null and size greater than 0");
		L2DomainRoutes routes = allRouting.getCreateDomainScoped(domain);
		RouterInfo info = new RouterInfo(domain);
		return new R2DomainRouterBuilder(info, routes, routes.getRoutesForDomain(), holder);
	}
	
	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, PortType type) {
		String totalPath = routerInfo.getPath()+path;
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
		
		StaticRoute route = new StaticRoute(getUniqueId(), new UrlPath(routerInfo, urlPath), fileSystemPath, isOnClassPath);
		staticRoutes.add(route);
		log.info("scope:'"+routerInfo+"' adding static route="+route.getFullPath()+" fileSystemPath="+route.getFileSystemPath());
		RouteMeta meta = new RouteMeta(route, injector.get(), currentPackage.get(), holder.getUrlEncoding());
		routes.addRoute(meta);
	}
	
	//only if you happen to create two webservers in two threads is this synchronized(unlikely scenario)
	private synchronized int getUniqueId() {
		return staticRouteIdCounter++;
	}

}
