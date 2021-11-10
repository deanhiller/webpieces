package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import org.webpieces.util.futures.XFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.exceptions.InternalErrorRouteFailedException;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.impl.RouterFutureUtil;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.logging.SupressedExceptionLog;

import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2ErrorCode;
import com.webpieces.http2.api.streaming.StreamWriter;

public class DScopedRouter extends EScopedRouter {

	private static final Logger log = LoggerFactory.getLogger(DScopedRouter.class);

	private ENotFoundRouter pageNotFoundRouter;
	private EInternalErrorRouter internalSvrErrorRouter;
	private RouterFutureUtil futureUtil;

	private FutureHelper futureHelper;

	public DScopedRouter(
			RouterInfo routerInfo, 
			Map<String, EScopedRouter> pathPrefixToNextRouter, 
			List<AbstractRouter> routers, 
			ENotFoundRouter notFoundRouter, 
			EInternalErrorRouter internalErrorRouter,
			RouterFutureUtil futureUtil,
			FutureHelper futureHelper
	) {
		super(futureUtil, routerInfo, pathPrefixToNextRouter, routers);
		this.pageNotFoundRouter = notFoundRouter;
		this.internalSvrErrorRouter = internalErrorRouter;
		this.futureUtil = futureUtil;
		this.futureHelper = futureHelper;
	}

	@Override
	public RouterStreamRef invokeRoute(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		
		RouterStreamRef streamRef = invokeRouteCatchNotFound(ctx, handler, subPath);
		
		XFuture<StreamWriter> writer = streamRef.getWriter()
				.handle( (r, t) -> {
					if(t == null)
						return XFuture.completedFuture(r);
		
					return tryRenderWebAppErrorControllerResult(ctx, handler, t);
				}).thenCompose(Function.identity());
		
		XFuture<StreamWriter> proxyWriter = writer.thenApply(w -> createProxy(w, ctx, handler));
		
		return new RouterStreamRef("dScopedRouter", proxyWriter, streamRef);
	}

	private StreamWriter createProxy(StreamWriter strWriter, RequestContext ctx, ProxyStreamHandle handler) {
		return new NonStreamingWebAppErrorProxy(futureHelper, strWriter, handler, ctx,
				(t) -> tryRenderWebAppErrorControllerResult(ctx, handler, t));
	}

	private XFuture<StreamWriter> tryRenderWebAppErrorControllerResult(RequestContext ctx, ProxyStreamHandle handler, Throwable t) {
		if(ExceptionWrap.isChannelClosed(t)) {
			//if the socket was closed before we responded, do not log a failure
			if(log.isTraceEnabled())
				log.trace("async exception due to socket being closed", t);
			return XFuture.<StreamWriter>completedFuture(new NullStreamWriter());
		}

		String failedRoute = "<Unknown Route>";
		if(t instanceof SpecificRouterInvokeException)
			failedRoute = ((SpecificRouterInvokeException) t).getMatchInfo()+"";

		if(!(t instanceof SimulateInternalError)) {
			log.error("There is three parts to this error message... request, route found, and the exception "
				+ "message.  You should\nread the exception message below  as well as the RouterRequest and RouteMeta.\n\n"
				+ctx.getRequest()+"\n\n"+failedRoute+".  \n\nNext, server will try to render apps 5xx page\n\n", t);
			SupressedExceptionLog.log(log, t);
		}


		//If it is a streaming, controller AND response has already been sent, we cannot render the web apps error controller
		//page so in that case, fail, and cancel the stream
		if(handler.hasSentResponseAlready()) {
			RstStreamFrame frame = new RstStreamFrame();
			frame.setKnownErrorCode(Http2ErrorCode.CANCEL);
			handler.cancel(frame);
			return XFuture.completedFuture(new NullStreamWriter());
		}
		
		return invokeWebAppErrorController(t, ctx, handler, failedRoute);
	}

	/**
	 * NOTE: We have to catch any exception from the method processNotFound so we can't catch and call internalServerError in this
	 * method without nesting even more!!! UGH, more nesting sucks
	 */
	private RouterStreamRef invokeRouteCatchNotFound(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		RouterStreamRef streamRef = super.invokeRoute(ctx, handler, subPath);

		XFuture<StreamWriter> writer = streamRef.getWriter()
				.handle( (r, t) -> {
					if(t == null)
						return XFuture.completedFuture(r);

					if (t instanceof NotFoundException)
						return notFound((NotFoundException) t, ctx, handler);
					return futureUtil.failedFuture(t);
				}).thenCompose(Function.identity());

		return new RouterStreamRef("DScopedNotFoundCheck", writer, streamRef);
	}

	private XFuture<StreamWriter> invokeWebAppErrorController(
			Throwable exc, RequestContext requestCtx, ProxyStreamHandle handler, Object failedRoute) {
		//This method is simply to translate the exception to InternalErrorRouteFailedException so higher levels
		//can determine if it was our bug or the web applications bug in it's Controller for InternalErrors
		return futureHelper.catchBlockWrap(
			() -> internalSvrErrorRouter.invokeErrorRoute(requestCtx, handler, exc),
			(t) -> convert(failedRoute, t)
		);
	}

	private InternalErrorRouteFailedException convert(Object failedRoute, Throwable t) {
		return new InternalErrorRouteFailedException(t, failedRoute);
	}
	
	private XFuture<StreamWriter> notFound(NotFoundException exc, RequestContext requestCtx, ProxyStreamHandle handler) {
		return futureHelper.catchBlockWrap(
			() -> pageNotFoundRouter.invokeNotFoundRoute(requestCtx, handler, exc),
			(e) -> new RuntimeException("NotFound Route had an exception", e)
		);
	}
	
	public String getDomain() {
		return routerInfo.getRouterId();
	}
}
