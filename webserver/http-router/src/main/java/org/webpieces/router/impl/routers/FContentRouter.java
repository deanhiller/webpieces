package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.model.RouteModuleInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.api.routebldr.ProcessCors;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForContent;

public class FContentRouter extends AbstractDynamicRouter implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private RouteModuleInfo moduleInfo;
	private final BodyContentBinder bodyContentBinder;
	private LoadedController loadedController;

	public FContentRouter(RouteInvoker routeInvoker, LoadedController loadedController, RouteModuleInfo moduleInfo, MatchInfo matchInfo, BodyContentBinder bodyContentBinder) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
		this.loadedController = loadedController;
		this.moduleInfo = moduleInfo;
		this.bodyContentBinder = bodyContentBinder;
	}
	
	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForContent(bodyContentBinder);
		String i18nBundleName = moduleInfo.getI18nBundleName();
		InvokeInfo invokeInfo = new InvokeInfo(ctx, handler, RouteType.CONTENT, loadedController, i18nBundleName);
		return routeInvoker.invokeContentController(invokeInfo, dynamicInfo, data);	
	}
	
	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

	@Override
	public RouteType getRouteType() {
		return RouteType.CONTENT;
	}

	public ProcessCors getCorsProcessor() {
		return moduleInfo.getCorsProcessor();
	}
}
