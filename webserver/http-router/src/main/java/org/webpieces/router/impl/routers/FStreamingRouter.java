package org.webpieces.router.impl.routers;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.ReversableRouter;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.InvokeInfo;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.router.impl.services.RouteData;
import org.webpieces.router.impl.services.RouteInfoForStream;

public class FStreamingRouter extends AbstractDynamicRouter implements ReversableRouter {

	private final RouteInvoker routeInvoker;
	private LoadedController loadedController;
	private String i18nBundleName;

	public FStreamingRouter(RouteInvoker routeInvoker, LoadedController loadedController, String i18nBundleName, MatchInfo matchInfo) {
		super(matchInfo);
		this.routeInvoker = routeInvoker;
		this.loadedController = loadedController;
		this.i18nBundleName = i18nBundleName;
	}

	@Override
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		RouteData data = new RouteInfoForStream();
		InvokeInfo invokeInfo = new InvokeInfo(ctx, handler, RouteType.STREAMING, loadedController, i18nBundleName);
		return routeInvoker.invokeStreamingController(invokeInfo, dynamicInfo, data);	
	}

	@Override
	public String getFullPath() {
		return matchInfo.getFullPath();
	}

}
