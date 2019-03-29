package org.webpieces.router.impl.model.bldr.data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.ErrorRoutes;
import org.webpieces.router.impl.NotFoundInfo;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.util.filters.Service;

public class Router extends ScopedRouter {

	private RouteInvoker2 routeInvoker;

	public Router(RouterInfo routerInfo, Map<String, ScopedRouter> pathPrefixToNextRouter, List<RouteMeta> routes, RouteInvoker2 routeInvoker) {
		super(routerInfo, pathPrefixToNextRouter, routes);
		this.routeInvoker = routeInvoker;
	}
	
	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb, ErrorRoutes errorRoutes,
			String subPath) {
		CompletableFuture<Void> future;
		try{
			future = super.invokeRoute(ctx, responseCb, subPath);
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		return future.handle((r, t) -> {
			if(t instanceof NotFoundException)
				return processNotFound(responseCb, ctx, (NotFoundException) t, errorRoutes, null);
			else if(t != null) {
				CompletableFuture<Void> failFuture = new CompletableFuture<>();
				failFuture.completeExceptionally(t);
				return failFuture;
			}
			
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
		
	}

	/**
	 * NEED to rescue this into calling webapps internal failure THEN rescue that into webpieces internal failure default(the catch-all)
	 *  
	 * @return
	 */
	public CompletableFuture<Void> processNotFound(ResponseStreamer responseCb, RequestContext requestCtx, NotFoundException e, ErrorRoutes errorRoutes, Object meta) {		
		NotFoundException exc = (NotFoundException) e;
		NotFoundInfo notFoundInfo = errorRoutes.fetchNotfoundRoute(exc);
		RouteMeta notFoundResult = notFoundInfo.getResult();
		RouterRequest overridenRequest = notFoundInfo.getReq();
		RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), overridenRequest);
		
		//http 404...(unless an exception happens in calling this code and that then goes to 500)
		return notFound(notFoundResult, notFoundInfo.getService(), exc, overridenCtx, responseCb);
	}
	
	private CompletableFuture<Void> notFound(RouteMeta notFoundResult, Service<MethodMeta, Action> service, NotFoundException exc, RequestContext requestCtx, ResponseStreamer responseCb) {
		try {
			MatchResult notFoundRes = new MatchResult(notFoundResult);
			return routeInvoker.invokeController(notFoundRes, requestCtx, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Void> futExc = new CompletableFuture<Void>();
			futExc.completeExceptionally(new RuntimeException("NotFound Route had an exception", e));
			return futExc;
		}
	}
	
	public String getDomain() {
		return routerInfo.getDomain();
	}
}
