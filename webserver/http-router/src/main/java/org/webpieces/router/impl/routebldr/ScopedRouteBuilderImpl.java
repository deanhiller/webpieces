package org.webpieces.router.impl.routebldr;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.CrudRouteIds;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.RouterFutureUtil;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.loader.BinderAndLoader;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.loader.MethodMetaAndController;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.EScopedRouter;
import org.webpieces.router.impl.routers.FContentRouter;
import org.webpieces.router.impl.routers.FHtmlRouter;
import org.webpieces.router.impl.routers.FStreamingRouter;
import org.webpieces.router.impl.routers.MatchInfo;
import org.webpieces.router.impl.services.SvcProxyForContent;
import org.webpieces.router.impl.services.SvcProxyForHtml;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.urlparse.RegExResult;
import org.webpieces.util.urlparse.RegExUtil;

public class ScopedRouteBuilderImpl extends SharedMatchUtil implements ScopedRouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(ScopedRouteBuilderImpl.class);

	protected final ResettingLogic resettingLogic;
	protected final RouteBuilderLogic holder;
	protected final RouterInfo routerInfo;
	private final Map<String, ScopedRouteBuilderImpl> pathToBuilder = new HashMap<>();

	private FutureHelper futureUtil;

	private RouterFutureUtil routerFutures;

	//private final List<StaticRouteMeta> staticRoutes = new ArrayList<>();
	//private List<StaticRoute> allStaticRoutes;
	
	public ScopedRouteBuilderImpl(RouterInfo routerInfo, RouteBuilderLogic holder, ResettingLogic resettingLogic, FutureHelper futureUtil, RouterFutureUtil routerFutures) {
		super(holder, resettingLogic);
		this.routerInfo = routerInfo;
		this.holder = holder;
		this.resettingLogic = resettingLogic;
		this.futureUtil = futureUtil;
		this.routerFutures = routerFutures;
	}

	@Override
	public void addContentRoute(Port port, HttpMethod method, String path, String controllerMethod) {
		addContentRoute(port, method, path, controllerMethod, null);
	}
	
	@Override
	public void addContentRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		UrlPath p = new UrlPath(routerInfo, path);
		RouteModuleInfo moduleInfo = CurrentRoutes.get();
		RouteInfo routeInfo = new RouteInfo(moduleInfo, controllerMethod);
		//MUST DO loadControllerIntoMeta HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		BinderAndLoader container = holder.getFinder().loadContentController(resettingLogic.getInjector(), routeInfo);
		
		MatchInfo matchInfo = createMatchInfo(p, port, method, holder.getUrlEncoding());
		LoadedController loadedController = container.getMetaAndController().getLoadedController();
		FContentRouter router = new FContentRouter(holder.getRouteInvoker2(), loadedController, moduleInfo, matchInfo, container.getBinder());
		SvcProxyForContent svc = new SvcProxyForContent(holder.getSvcProxyLogic(), futureUtil);
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, container.getMetaAndController(), svc);
		
		newDynamicRoutes.add(routerAndInfo);
		if(routeId != null) //if there is a routeId, then add the reverse mapping
			resettingLogic.getReverseRoutes().addRoute(routeId, router);

		log.info("scope:'"+routerInfo+"' added content route="+matchInfo+" method="+routeInfo.getControllerMethodString());
	}
	
	@Override
	public void addStreamRoute(Port port, HttpMethod method, String path, String controllerMethod) {
		addStreamRoute(port, method, path, controllerMethod, null);
	}
	
	@Override
	public void addStreamRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		UrlPath p = new UrlPath(routerInfo, path);
		RouteModuleInfo moduleInfo = CurrentRoutes.get();
		RouteInfo routeInfo = new RouteInfo(moduleInfo, controllerMethod);

		//MUST DO loadControllerIntoMeta HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		MethodMetaAndController container = holder.getFinder().loadGenericController(resettingLogic.getInjector(), routeInfo);
		
		MatchInfo matchInfo = createMatchInfo(p, port, method, holder.getUrlEncoding());
		FStreamingRouter router = new FStreamingRouter(holder.getRouteInvoker2(), container.getLoadedController(), moduleInfo.getI18nBundleName(), matchInfo);
		SvcProxyForContent svc = new SvcProxyForContent(holder.getSvcProxyLogic(), futureUtil);
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, container, svc);
		
		newDynamicRoutes.add(routerAndInfo);
		if(routeId != null) //if there is a routeId, then add the reverse mapping
			resettingLogic.getReverseRoutes().addRoute(routeId, router);
		
		log.info("scope:'"+routerInfo+"' added content route="+matchInfo+" method="+routeInfo.getControllerMethodString());		
	}
	
	private MatchInfo createMatchInfo(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding) {
		RegExResult result = RegExUtil.parsePath(urlPath.getSubPath());
		Pattern patternToMatch = Pattern.compile(result.regExToMatch);
		List<String> pathParamNames = result.argNames;
		return new MatchInfo(urlPath, exposedPort, httpMethod, urlEncoding, patternToMatch, pathParamNames);
	}
	
	@Override
	public void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId) {
		boolean checkSecureToken = false;
		if(method == HttpMethod.POST)
			checkSecureToken = true;
		addRoute(port, method, path, controllerMethod, routeId, checkSecureToken);
	}

	@Override
	public void addRoute(Port port, HttpMethod method, String path, String controllerMethod, RouteId routeId,
			boolean checkToken) {
		if(!controllerMethod.contains("."))
			throw new IllegalArgumentException("controllerMethod='"+controllerMethod+" does not contain a '.' which is required separating class name and method");
		UrlPath p = new UrlPath(routerInfo, path);

		boolean isPostOnly = method == HttpMethod.POST;
		RouteModuleInfo moduleInfo = CurrentRoutes.get();
		RouteInfo routeInfo = new RouteInfo(moduleInfo, controllerMethod);

		//MUST DO loadControllerIntoMetat HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		MethodMetaAndController metaAndController = holder.getFinder().loadHtmlController(resettingLogic.getInjector(), routeInfo, isPostOnly);
		
		MatchInfo matchInfo = createMatchInfo(p, port, method, holder.getUrlEncoding());
		LoadedController loadedController = metaAndController.getLoadedController();
		FHtmlRouter router = new FHtmlRouter(holder.getRouteInvoker2(), loadedController, moduleInfo.getI18nBundleName(), matchInfo, checkToken);	
		SvcProxyForHtml svc = new SvcProxyForHtml(holder.getSvcProxyLogic(), futureUtil);
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, metaAndController, svc);
		
		newDynamicRoutes.add(routerAndInfo);
		resettingLogic.getReverseRoutes().addRoute(routeId, router);
		
		log.info("scope:'"+routerInfo+"' added route="+matchInfo+" method="+routeInfo.getControllerMethodString());
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
	public void addCrud(Port port, String entity, String controller, CrudRouteIds routeIds) {
		RouteId listRoute = routeIds.getListRoute();
		RouteId addRoute = routeIds.getAddRoute();
		RouteId editRoute = routeIds.getEditRoute(); 
		RouteId postSaveRoute = routeIds.getPostSaveRoute();
		RouteId confirmDelete = routeIds.getConfirmDelete();
		RouteId postDeleteRoute = routeIds.getPostDeleteRoute();
		
		String entityWithCapital = entity.substring(0, 1).toUpperCase() + entity.substring(1);
		addRoute(port, GET , "/"+entity+"/list",        controller+"."+entity+"List", listRoute);
		addRoute(port, GET , "/"+entity+"/new",         controller+"."+entity+"AddEdit", addRoute);
		addRoute(port, GET , "/"+entity+"/edit/{id}",   controller+"."+entity+"AddEdit", editRoute);
		addRoute(port, POST, "/"+entity+"/post",        controller+".postSave"+entityWithCapital, postSaveRoute);

		//get the confirm delete page
		addRoute(port, GET,  "/"+entity+"/confirmdelete/{id}", controller+".confirmDelete"+entityWithCapital, confirmDelete);
		//NOTE: Browsers don't support DELETE.  POST might make more sense here for delete but GET is way way less html
		//code(ok, 1 line instead of 3).  There are hacks with javascript to support DELETE but seriously, we should just
		//KISS and YAGNI (google that if you don't know).  
		//HOWEVER, If you don't like this, copy and paste this method and modify to be a POST OR DELETE and add the 
		//javascript for next time
		addRoute(port, POST, "/"+entity+"/delete/{id}", controller+".postDelete"+entityWithCapital, postDeleteRoute);
	}
	
	/**
	 * Note, if someone passes in /car/civic, we have to create a ScopedRouteBuilder and
	 * then call getScopedRouteBuilder("/civic") on that so that lookups work when they
	 * do getScopedRouteBuilder("/car") in another module.
	 */
	@Override
	public ScopedRouteBuilder getScopedRouteBuilder(String fullPath) {
		if(fullPath == null)
			throw new IllegalArgumentException("path cannot be null");
		else if(!fullPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");
		else if(fullPath.endsWith("/"))
			throw new IllegalArgumentException("path must not end with /");
		else if(fullPath.length() <= 1)
			throw new IllegalArgumentException("path size must be greater than 1");
		
		String[] split = splitInTwo(fullPath);
		String path = fullPath;
		if(split != null)
			path = split[0];
		
		ScopedRouteBuilderImpl r = pathToBuilder.get(path);
		if(r == null) {
			r = new ScopedRouteBuilderImpl(new RouterInfo(routerInfo.getRouterId(), routerInfo.getPath()+path), holder, resettingLogic, futureUtil, routerFutures);
			pathToBuilder.put(path, r);
		}
		
		if(split == null)
			return r;
		else
			return r.getScopedRouteBuilder(split[1]);
	}
	
	private String[] splitInTwo(String fullPath) {
		if(!fullPath.startsWith("/"))
			throw new IllegalArgumentException("fullPath should start with a / but did not");
		
		int indexOf = fullPath.indexOf("/", 1);
		if(indexOf < 0)
			return null;
		
		String path = fullPath.substring(0, indexOf);
		String leftover = fullPath.substring(indexOf);
		return new String[] {path, leftover};
	}
	

	
	public EScopedRouter build(List<FilterInfo<?>> routeFilters) {
		List<AbstractRouter> routers = buildRoutes(routeFilters);
		
		Map<String, EScopedRouter> pathToRouter = buildScopedRouters(routeFilters);
		
		return new EScopedRouter(routerFutures, routerInfo, pathToRouter, routers);
	}

	protected Map<String, EScopedRouter> buildScopedRouters(List<FilterInfo<?>> routeFilters) {
		Map<String, EScopedRouter> pathToRouter = new HashMap<>();
		for(Entry<String, ScopedRouteBuilderImpl> entry : pathToBuilder.entrySet()) {
			EScopedRouter router2 = entry.getValue().build(routeFilters);
			pathToRouter.put(entry.getKey(), router2);
		}
		return pathToRouter;
	}

	protected List<AbstractRouter> buildRoutes(List<FilterInfo<?>> routeFilters) {
		List<AbstractRouter> routers = super.buildRoutes(routeFilters);
		return routers;
	}

}
