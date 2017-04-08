package org.webpieces.router.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webpieces.router.api.routing.PortType;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class R1RouterBuilder extends AbstractDomainBuilder  {

	private static final Logger log = LoggerFactory.getLogger(R2DomainRouterBuilder.class);

	private L1AllRouting allRouting;

	private List<StaticRoute> staticRoutes = new ArrayList<>();
	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();
	
	public R1RouterBuilder(RouterInfo info, L1AllRouting allRouting, LogicHolder holder, boolean isHttpsOnlyRoutes) {
		super(info, allRouting.getMainRoutes(), allRouting.getMainRoutes().getRoutesForDomain(), holder, isHttpsOnlyRoutes);
		this.allRouting = allRouting;
	}

	public L1AllRouting getRouterInfo() {
		return allRouting;
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
		
		StaticRoute route = new StaticRoute(new UrlPath(routerInfo, urlPath), fileSystemPath, isOnClassPath, holder.getCachedCompressedDirectory());
		staticRoutes.add(route);
		log.info("scope:'"+routerInfo+"' adding static route="+route.getFullPath()+" fileSystemPath="+route.getFileSystemPath());
		RouteMeta meta = new RouteMeta(route, holder.getInjector(), currentPackage.get(), holder.getUrlEncoding());
		allRouting.addStaticRoute(meta);
	}
	
	public void applyFilters(WebAppMeta rm) {
		ReverseRoutes reverseRoutes = holder.getReverseRoutes();
		Collection<RouteMeta> metas = reverseRoutes.getAllRouteMetas();
		for(RouteMeta meta : metas) {
			String path = meta.getRoute().getFullPath();
			List<FilterInfo<?>> filters = findMatchingFilters(path, meta.getRoute().isHttpsRoute());
			meta.setFilters(filters);
		}
		
		List<L2DomainRoutes> allDomains = allRouting.getAllDomains();
		
		for(L2DomainRoutes domainRoutes : allDomains) {
			applyFilters(domainRoutes, rm);
		}
	}

	private void applyFilters(L2DomainRoutes domainRoutes, WebAppMeta rm) {
		String domain = domainRoutes.getDomain();
		if(domain == null)
			domain = "ALLOTHER";

		RouteMeta notFoundMeta = domainRoutes.getPageNotFoundRoute();

		RouteMeta internalErrorMeta = domainRoutes.getInternalSvrErrorRoute();
		
		if(notFoundMeta == null)
			throw new IllegalStateException("router.setNotFoundRoute MUST be called for domain="+domain+"  Modules="+rm.getRouteModules());
		else if(internalErrorMeta == null)
			throw new IllegalStateException("router.setInternalSvrErrorRoute MUST be called for domain="+domain+".  Modules="+rm.getRouteModules());
			
		internalErrorMeta.setFilters(internalErrorFilters);
		notFoundMeta.setFilters(notFoundFilters);
	}

	public List<StaticRoute> getStaticRoutes() {
		return staticRoutes;
	}

	public List<FilterInfo<?>> findNotFoundFilters(String path, boolean isHttps) {
		List<FilterInfo<?>> matchingFilters = new ArrayList<>();
		for(FilterInfo<?> info : notFoundFilters) {
			if(!info.securityMatch(isHttps))
				continue; //skip this filter
			
			matchingFilters.add(0, info);
		}
		return matchingFilters;
	}
	
	public List<FilterInfo<?>> findMatchingFilters(String path, boolean isHttps) {
		List<FilterInfo<?>> matchingFilters = new ArrayList<>();
		for(FilterInfo<?> info : routeFilters) {
			if(!info.securityMatch(isHttps))
				continue; //skip this filter
			
			Pattern patternToMatch = info.getPatternToMatch();
			Matcher matcher = patternToMatch.matcher(path);
			if(matcher.matches()) {
				matchingFilters.add(0, info);
			}
		}
		return matchingFilters;
	}
	
	public void loadNotFoundAndErrorFilters() {
		List<L2DomainRoutes> allDomains = allRouting.getAllDomains();
		
		for(L2DomainRoutes domainRoutes : allDomains) {
			loadFilters(domainRoutes);
		}
		
	}
	private void loadFilters(L2DomainRoutes domainRoutes) {
		RouteMeta notFound = domainRoutes.getPageNotFoundRoute();
		RouteMeta internalErrorMeta = domainRoutes.getInternalSvrErrorRoute();

		holder.getFinder().loadFiltersIntoMeta(notFound, notFound.getFilters(), true);
		holder.getFinder().loadFiltersIntoMeta(internalErrorMeta, internalErrorMeta.getFilters(), true);
	}

}
