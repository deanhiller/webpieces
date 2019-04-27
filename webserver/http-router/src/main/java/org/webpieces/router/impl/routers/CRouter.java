package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.util.filters.ExceptionUtil;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.logging.SupressedExceptionLog;

public class CRouter extends DScopedRouter {

	private static final Logger log = LoggerFactory.getLogger(CRouter.class);

	private DNotFoundRouter pageNotFoundRouter;
	private DInternalErrorRouter internalSvrErrorRouter;

	public CRouter(
			RouterInfo routerInfo, 
			Map<String, DScopedRouter> pathPrefixToNextRouter, 
			List<AbstractRouter> routers, 
			DNotFoundRouter notFoundRouter, 
			DInternalErrorRouter internalErrorRouter
	) {
		super(routerInfo, pathPrefixToNextRouter, routers);
		this.pageNotFoundRouter = notFoundRouter;
		this.internalSvrErrorRouter = internalErrorRouter;
	}

	@Override
	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb, String subPath) {
		CompletableFuture<Void> future = invokeRouteCatchNotFound(ctx, responseCb, subPath).handle((r, t) -> {
			if(t != null) {
				String failedRoute = "<Unknown Route>";
				if(t instanceof SpecificRouterInvokeException)
					failedRoute = ((SpecificRouterInvokeException) t).getMatchInfo()+"";
				
				return internalServerError(t, ctx, responseCb, failedRoute);
			}
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
		
		return future;
	}
	/**
	 * NOTE: We have to catch any exception from the method processNotFound so we can't catch and call internalServerError in this
	 * method without nesting even more!!! UGH, more nesting sucks
	 */
	private CompletableFuture<Void> invokeRouteCatchNotFound(RequestContext ctx, ResponseStreamer responseCb, String subPath) {

		CompletableFuture<Void> future = ExceptionUtil.wrap(
			() -> super.invokeRoute(ctx, responseCb, subPath)
		);
		
		return future.handle((r, t) -> {
			if(t instanceof NotFoundException)
				return notFound((NotFoundException)t, ctx, responseCb);
			else if(t != null) {
				return ExceptionUtil.<Void>failedFuture(t);
			}
			
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
	}

	private CompletableFuture<Void> internalServerError(
			Throwable exc, RequestContext requestCtx, ResponseStreamer responseCb, Object failedRoute) {
		//This method is simply to translate the exception to InternalErrorRouteFailedException so higher levels
		//can determine if it was our bug or the web applications bug in it's Controller for InternalErrors
		CompletableFuture<Void> future = ExceptionUtil.wrap(
			() -> internalServerErrorImpl(exc, requestCtx, responseCb, failedRoute)
		);
				
		return future.handle((r, t) -> {
			if(t != null) {
				CompletableFuture<Void> future1 = new CompletableFuture<Void>();
				future1.completeExceptionally(new InternalErrorRouteFailedException(t, failedRoute));
				return future1;
			}
			
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
	}
	
	private CompletableFuture<Void> internalServerErrorImpl(
		Throwable exc, RequestContext requestCtx, ResponseStreamer responseCb, Object failedRoute) {
		log.error("There is three parts to this error message... request, route found, and the exception "
				+ "message.  You should\nread the exception message below  as well as the RouterRequest and RouteMeta.\n\n"
				+requestCtx.getRequest()+"\n\n"+failedRoute+".  \n\nNext, server will try to render apps 5xx page\n\n", exc);
		SupressedExceptionLog.log(exc);
		
		return internalSvrErrorRouter.invokeErrorRoute(requestCtx, responseCb);
	}
	
	private CompletableFuture<Void> notFound(NotFoundException exc, RequestContext requestCtx, ResponseStreamer responseCb) {
		return ExceptionUtil.wrap(
			() -> pageNotFoundRouter.invokeNotFoundRoute(requestCtx, responseCb, exc),
			(e) -> new RuntimeException("NotFound Route had an exception", e)
		);
	}
	
	public String getDomain() {
		return routerInfo.getDomain();
	}
}
