package org.webpieces.router.impl.routebldr;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.ContentTypeRouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.ResettingLogic;
import org.webpieces.router.impl.UrlPath;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.BinderAndLoader;
import org.webpieces.router.impl.model.RouteBuilderLogic;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.routers.AbstractRouter;
import org.webpieces.router.impl.routers.DContentTypeRouter;
import org.webpieces.router.impl.routers.FContentRouter;
import org.webpieces.router.impl.routers.MatchInfo;
import org.webpieces.router.impl.services.SvcProxyForContent;

public class ContentTypeBuilderImpl extends SharedMatchUtil implements ContentTypeRouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(ContentTypeBuilderImpl.class);

	private RouterInfo routerInfo;
	private RouteBuilderLogic holder;
	private ResettingLogic resettingLogic;

	private List<FilterInfo<?>> routeFilters = new ArrayList<>();
	private final List<RouterAndInfo> newDynamicRoutes = new ArrayList<>();

	public ContentTypeBuilderImpl(RouterInfo routerInfo, RouteBuilderLogic holder, ResettingLogic resettingLogic) {
		super(holder, resettingLogic);
		this.routerInfo = routerInfo;
		this.holder = holder;
		this.resettingLogic = resettingLogic;
		throw new UnsupportedOperationException("Not supported yet, but coming soon for grpc and content types that need the root url / to work.  In fact, it's done except for testing");
	}

	@Override
	public void addRoute(String path, String controllerMethod) {
		UrlPath p = new UrlPath(routerInfo, path);
		RouteInfo routeInfo = new RouteInfo(CurrentPackage.get(), controllerMethod);
		//MUST DO loadControllerIntoMeta HERE so stack trace has customer's line in it so he knows EXACTLY what 
		//he did wrong when reading the exception!!
		BinderAndLoader container = holder.getFinder().loadContentController(resettingLogic.getInjector(), routeInfo, true);
		
		MatchInfo matchInfo = createMatchInfo(p, Port.HTTPS, HttpMethod.POST, holder.getUrlEncoding());
		FContentRouter router = new FContentRouter(holder.getRouteInvoker2(), matchInfo, container.getBinder());
		SvcProxyForContent svc = new SvcProxyForContent(holder.getSvcProxyLogic());
		RouterAndInfo routerAndInfo = new RouterAndInfo(router, routeInfo, RouteType.HTML, container.getLoadedController(), svc);
		
		newDynamicRoutes.add(routerAndInfo);
		
		//There is no routeId...
		//if(routeId != null) //if there is a routeId, then add the reverse mapping
		//	resettingLogic.getReverseRoutes().addRoute(routeId, router);

		log.info("scope:'"+routerInfo+"' added content route="+matchInfo+" method="+routeInfo.getControllerMethodString());
	}
	
	private MatchInfo createMatchInfo(UrlPath urlPath, Port exposedPort, HttpMethod httpMethod, Charset urlEncoding) {
		RegExResult result = RegExUtil.parsePath(urlPath.getSubPath());
		Pattern patternToMatch = Pattern.compile(result.regExToMatch);
		List<String> pathParamNames = result.argNames;
		return new MatchInfo(urlPath, exposedPort, httpMethod, urlEncoding, patternToMatch, pathParamNames);
	}
	
	@Override
	public <T> void addFilter(String path, Class<? extends RouteFilter<T>> filter, T initialConfig, int filterApplyLevel) {
		FilterInfo<T> info = new FilterInfo<>(path, filter, initialConfig, FilterPortType.HTTPS_FILTER, filterApplyLevel);
		routeFilters.add(info);
	}

	public DContentTypeRouter buildRouter() {
		List<AbstractRouter> routers = buildRoutes(routeFilters);

		return new DContentTypeRouter(routers);
	}

}
