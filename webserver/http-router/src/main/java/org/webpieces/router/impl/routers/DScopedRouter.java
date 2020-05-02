package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.InternalErrorRouteFailedException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.util.futures.ExceptionUtil;
import org.webpieces.util.logging.SupressedExceptionLog;

public class DScopedRouter extends EScopedRouter {

	private static final Logger log = LoggerFactory.getLogger(DScopedRouter.class);

	private ENotFoundRouter pageNotFoundRouter;
	private EInternalErrorRouter internalSvrErrorRouter;

	public DScopedRouter(
			RouterInfo routerInfo, 
			Map<String, EScopedRouter> pathPrefixToNextRouter, 
			List<AbstractRouter> routers, 
			ENotFoundRouter notFoundRouter, 
			EInternalErrorRouter internalErrorRouter
	) {
		super(routerInfo, pathPrefixToNextRouter, routers);
		this.pageNotFoundRouter = notFoundRouter;
		this.internalSvrErrorRouter = internalErrorRouter;
	}

	@Override
	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb, String subPath) {
		CompletableFuture<Void> future = invokeRouteCatchNotFound(ctx, responseCb, subPath);
		
		return future.handle((r, t) -> {
			if(t != null) {
				if(ExceptionWrap.isChannelClosed(t)) {
					//if the socket was closed before we responded, do not log a failure
					if(log.isTraceEnabled())
						log.trace("async exception due to socket being closed", t);
					return CompletableFuture.<Void>completedFuture(null);
				}
				
				String failedRoute = "<Unknown Route>";
				if(t instanceof SpecificRouterInvokeException)
					failedRoute = ((SpecificRouterInvokeException) t).getMatchInfo()+"";
				
				log.error("There is three parts to this error message... request, route found, and the exception "
						+ "message.  You should\nread the exception message below  as well as the RouterRequest and RouteMeta.\n\n"
						+ctx.getRequest()+"\n\n"+failedRoute+".  \n\nNext, server will try to render apps 5xx page\n\n", t);
				SupressedExceptionLog.log(log, t);
				

				CompletableFuture<Void> retVal = invokeWebAppErrorController(t, ctx, responseCb, failedRoute);
				return retVal;
			}
			return CompletableFuture.completedFuture(r); 
		}).thenCompose(Function.identity());
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

	private CompletableFuture<Void> invokeWebAppErrorController(
			Throwable exc, RequestContext requestCtx, ResponseStreamer responseCb, Object failedRoute) {
		//This method is simply to translate the exception to InternalErrorRouteFailedException so higher levels
		//can determine if it was our bug or the web applications bug in it's Controller for InternalErrors
		return ExceptionUtil.wrapException(
			() -> internalSvrErrorRouter.invokeErrorRoute(requestCtx, responseCb),
			(t) -> convert(failedRoute, t)
		);
	}

	private InternalErrorRouteFailedException convert(Object failedRoute, Throwable t) {
		return new InternalErrorRouteFailedException(t, failedRoute);
	}
	
	private CompletableFuture<Void> notFound(NotFoundException exc, RequestContext requestCtx, ResponseStreamer responseCb) {
		return ExceptionUtil.wrap(
			() -> pageNotFoundRouter.invokeNotFoundRoute(requestCtx, responseCb, exc),
			(e) -> new RuntimeException("NotFound Route had an exception", e)
		);
	}
	
	public String getDomain() {
		return routerInfo.getRouterId();
	}
}
