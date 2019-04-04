package org.webpieces.router.impl.routebldr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.AbstractRouteMeta;
import org.webpieces.router.impl.BaseRouteInfo;
import org.webpieces.router.impl.FilterInfo;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.svc.MethodMeta;
import org.webpieces.router.impl.loader.svc.SvcProxyFixedRoutes;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.InternalErrorRouter;
import org.webpieces.router.impl.routers.NotFoundRouter;
import org.webpieces.router.impl.routers.Router;
import org.webpieces.router.impl.routers.ScopedRouter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class RouteBuilderImpl extends ScopedRouteBuilderImpl implements RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(RouteBuilderImpl.class);

	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private List<FilterInfo<?>> notFoundFilters = new ArrayList<>();
	private List<FilterInfo<?>> internalErrorFilters = new ArrayList<>();

	private RouteInfo pageNotFoundInfo;
	private RouteInfo internalErrorInfo;

	private LoadedController notFoundControllerInst;
	private LoadedController internalErrorController;
	
	public RouteBuilderImpl(String domain, List<StaticRoute> allStaticRoutes, RouteBuilderLogic holder, ResettingLogic resettingLogic) {
		super(new RouterInfo(domain, ""), allStaticRoutes, holder, resettingLogic);
	}

	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type) {
		FilterInfo<T> info = new FilterInfo<>(path, filter, initialConfig, type);
		routeFilters.add(info);
	}

	@Override
	public <T> void addNotFoundFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type);
		notFoundFilters.add(info);		
	}

	@Override
	public <T> void addInternalErrorFilter(Class<? extends RouteFilter<T>> filter, T initialConfig, FilterPortType type) {
		FilterInfo<T> info = new FilterInfo<>("", filter, initialConfig, type);
		internalErrorFilters.add(info);		
	}

	@Override
	public void setPageNotFoundRoute(String controllerMethod) {
		if(pageNotFoundInfo != null)
			throw new IllegalStateException("Page Not found for domain="+routerInfo.getDomain()+" was already set.  cannot set again.  previous="+pageNotFoundInfo);
		RouteInfo route = new RouteInfo(CurrentPackage.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding PAGE_NOT_FOUND route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.notFoundControllerInst = holder.getFinder().loadGenericController(resettingLogic.getInjector(), route, true);
		this.pageNotFoundInfo = route;
	}

	@Override
	public void setInternalErrorRoute(String controllerMethod) {
		if(internalErrorInfo != null)
			throw new IllegalStateException("Internal Error Route for domain="+routerInfo.getDomain()+" was already set.  cannot set again");
		RouteInfo route = new RouteInfo(CurrentPackage.get(), controllerMethod);
		log.info("scope:'"+routerInfo+"' adding INTERNAL_SVR_ERROR route method="+route.getControllerMethodString());
		
		//MUST DO loadController HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		this.internalErrorController = holder.getFinder().loadGenericController(resettingLogic.getInjector(), route, true);
		this.internalErrorInfo = route;
	}

	public Router buildRouter() {
		List<AbstractRouteMeta> routes = buildRoutes(routeFilters);
		List<AbstractRouter> routers = buildRoutes2(routeFilters);

		Map<String, ScopedRouter> pathToRouter = buildScopedRouters(routeFilters);

		SvcProxyFixedRoutes svcProxy = new SvcProxyFixedRoutes(holder.getSvcProxyLogic().getServiceInvoker());

		BaseRouteInfo notFoundRoute = new BaseRouteInfo(
				resettingLogic.getInjector(), pageNotFoundInfo, 
				svcProxy, notFoundFilters,
				RouteType.NOT_FOUND);
		BaseRouteInfo internalErrorRoute = new BaseRouteInfo(
				resettingLogic.getInjector(), internalErrorInfo, 
				svcProxy, internalErrorFilters,
				RouteType.INTERNAL_SERVER_ERROR);

		Service<MethodMeta, Action> svc = holder.getFinder().loadFilters(internalErrorRoute, true);
		InternalErrorRouter internalErrorRouter = new InternalErrorRouter(holder.getRouteInvoker2(), internalErrorRoute, internalErrorController, svc);

		//NOTE: We do NOT create a Service<MethodMeta, Action> here with Filters
		//Service<MethodMeta, Action> must be created on demand(it's cheap operation) because filters must pattern match
		//on the request coming in and then we form the service per request
		//WE could turn this off and expose an addGlobalNotFoundFilter that applies always to every not found????
		//it's faster performance due to know pattern matching every request I guess
		NotFoundRouter notFoundRouter = new NotFoundRouter(holder.getRouteInvoker2(), notFoundRoute, notFoundControllerInst);
		return new Router(routerInfo, pathToRouter, routes, routers, notFoundRouter, internalErrorRouter);
	}

}
