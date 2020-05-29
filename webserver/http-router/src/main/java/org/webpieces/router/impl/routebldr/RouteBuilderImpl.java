package org.webpieces.router.impl.routebldr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.DScopedRouter;
import org.webpieces.router.impl.routers.EInternalErrorRouter;
import org.webpieces.router.impl.routers.ENotFoundRouter;
import org.webpieces.router.impl.routers.EScopedRouter;
import org.webpieces.router.impl.services.SvcProxyFixedRoutes;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;

public class RouteBuilderImpl extends ScopedRouteBuilderImpl implements RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(RouteBuilderImpl.class);

	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();

	private RouteInfo pageNotFoundInfo;
	private RouteInfo internalErrorInfo;

	private LoadedController notFoundControllerInst;
	private LoadedController internalErrorController;

	private FutureHelper futureUtil;
	
	public RouteBuilderImpl(String id, RouteBuilderLogic holder, ResettingLogic resettingLogic, FutureHelper futureUtil) {
		super(new RouterInfo(id, ""), holder, resettingLogic, futureUtil);
		this.futureUtil = futureUtil;
	}

	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>(path, filter, initialConfig, type, filterApplyLevel);
		routeFilters.add(info);
	}
	
	@Override
	public <T> void addPackageFilter(String regEx, Class<? extends RouteFilter<T>> filter, T initialConfig,
			FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>(regEx, true, filter, initialConfig, type, filterApplyLevel);
		routeFilters.add(info);		
	}
	
	@Override
	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type, filterApplyLevel);
		notFoundFilters.add(info);		
	}

	@Override
	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type, filterApplyLevel);
		internalErrorFilters.add(info);		
	}

	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		if(pageNotFoundInfo != null)
			throw new IllegalStateException("Page Not found for domain="+routerInfo.getRouterId()+" was already set.  cannot set again.  previous="+pageNotFoundInfo);
		RouteInfo route = new RouteInfo(CurrentPackage.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding PAGE_NOT_FOUND route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.notFoundControllerInst = holder.getFinder().loadNotFoundController(resettingLogic.getInjector(), route, true);
		this.pageNotFoundInfo = route;
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		if(internalErrorInfo != null)
			throw new IllegalStateException("Internal Error Route for domain="+routerInfo.getRouterId()+" was already set.  cannot set again");
		RouteInfo route = new RouteInfo(CurrentPackage.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding INTERNAL_SVR_ERROR route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.internalErrorController = holder.getFinder().loadErrorController(resettingLogic.getInjector(), route, true);
		this.internalErrorInfo = route;
	}

	public DScopedRouter buildRouter() {
		if(pageNotFoundInfo == null)
			throw new IllegalStateException("Client did not call setPageNotFoundRoute for router="+routerInfo+" and that's required to catch stray not founds");
		else if(internalErrorInfo == null)
			throw new IllegalStateException("Client did not call setInternalErrorRoute for router="+routerInfo+" and that's required to catch stray bugs in your application");
		
		List<AbstractRouter> routers = super.buildRoutes(routeFilters);

		Map<String, EScopedRouter> pathToRouter = buildScopedRouters(routeFilters);

		SvcProxyFixedRoutes svcProxy = new SvcProxyFixedRoutes(holder.getSvcProxyLogic().getServiceInvoker(), futureUtil);

		BaseRouteInfo notFoundRoute = new BaseRouteInfo(
				resettingLogic.getInjector(), pageNotFoundInfo, 
				svcProxy, notFoundFilters,
				RouteType.NOT_FOUND);
		BaseRouteInfo internalErrorRoute = new BaseRouteInfo(
				resettingLogic.getInjector(), internalErrorInfo, 
				svcProxy, internalErrorFilters,
				RouteType.INTERNAL_SERVER_ERROR);

		Service<MethodMeta, Action> svc = holder.getFinder().loadFilters(internalErrorRoute, true);
		EInternalErrorRouter internalErrorRouter = new EInternalErrorRouter(holder.getRouteInvoker2(), internalErrorRoute, internalErrorController, svc);

		//NOTE: We do NOT create a Service<MethodMeta, Action> here with Filters
		//Service<MethodMeta, Action> must be created on demand(it's cheap operation) because filters must pattern match
		//on the request coming in and then we form the service per request
		//WE could turn this off and expose an addGlobalNotFoundFilter that applies always to every not found????
		//it's faster performance due to know pattern matching every request I guess
		ENotFoundRouter notFoundRouter = new ENotFoundRouter(holder.getRouteInvoker2(), notFoundRoute, notFoundControllerInst);
		return new DScopedRouter(routerInfo, pathToRouter, routers, notFoundRouter, internalErrorRouter, futureUtil);
	}

}
