package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.RenderContent;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.logging.SupressedExceptionLog;

@Singleton
public class RouteInvoker {

	private static final Logger log = LoggerFactory.getLogger(RouteInvoker.class);
	//initialized in init() method and re-initialized in dev mode from that same method..
	private ReverseRoutes reverseRoutes;
	private ObjectToParamTranslator reverseTranslator;
	private RouterConfig config;

	@Inject
	public RouteInvoker(
		ObjectToParamTranslator reverseTranslator,
		RouterConfig config
	) {
		this.reverseTranslator = reverseTranslator;
		this.config = config;
	}

	public CompletableFuture<Void> invoke(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb, ErrorRoutes errorRoutes) {
		CompletableFuture<Void> future;
		try{
			future = invoke2(result, requestCtx, responseCb, errorRoutes);
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		return future.handle((r, t) -> {
			if(t instanceof NotFoundException)
				return processNotFound(responseCb, requestCtx, (NotFoundException) t, errorRoutes, null);
			else if(t != null) {
				CompletableFuture<Void> failFuture = new CompletableFuture<>();
				failFuture.completeExceptionally(t);
				return failFuture;
			}
			
			return CompletableFuture.completedFuture(r);
		}).thenCompose(Function.identity());
	}
	
	public CompletableFuture<Void> invoke2(MatchResult result, RequestContext requestCtx, ResponseStreamer responseCb, ErrorRoutes errorRoutes) {

		//This makes us consistent with other NotFoundExceptions and without the cost of 
		//throwing an exception and filling in stack trace...
		//We could convert the exc. to FastException and override method so stack is not filled in but that
		//can get very annoying
		RouteMeta meta = result.getMeta();
		Route route = meta.getRoute();
		RouteType routeType = route.getRouteType();
		if(routeType == RouteType.NOT_FOUND) {
			CompletableFuture<Void> future = new CompletableFuture<Void>();
			future.completeExceptionally(new NotFoundException("route not found"));
			return future;
		}
		
		return invokeImpl(result, meta.getService222(), requestCtx, responseCb);
	}

	public CompletableFuture<Void> processNotFound(ResponseStreamer responseCb, RequestContext requestCtx, NotFoundException e, ErrorRoutes errorRoutes, Object meta) {		
		NotFoundException exc = (NotFoundException) e;
		NotFoundInfo notFoundInfo = errorRoutes.fetchNotfoundRoute(exc);
		RouteMeta notFoundResult = notFoundInfo.getResult();
		RouterRequest overridenRequest = notFoundInfo.getReq();
		RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), overridenRequest);
		
		//http 404...(unless an exception happens in calling this code and that then goes to 500)
		return notFound(notFoundResult, notFoundInfo.getService(), exc, overridenCtx, responseCb);
	}
	
	public Void processException(ResponseStreamer responseCb, RequestContext requestCtx, Throwable e, ErrorRoutes errorRoutes, Object meta) {
		//If this fails, then the users 5xx page is messed up and we then render our own 5xx page
		CompletableFuture<Void> future = internalServerError(errorRoutes, e, requestCtx, responseCb, meta);
		future.exceptionally(finalExc -> finalFailure(responseCb, finalExc, requestCtx));
		return null;
	}
	
	public Void finalFailure(ResponseStreamer responseCb, Throwable e, RequestContext requestCtx) {
		log.error("This is a final(secondary failure) trying to render the Internal Server Error Route", e); 
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, null, responseCb);
		processor.failureRenderingInternalServerErrorPage(e);
		return null;
	}
	
	private CompletableFuture<Void> notFound(RouteMeta notFoundResult, Service<MethodMeta, Action> service, NotFoundException exc, RequestContext requestCtx, ResponseStreamer responseCb) {
		try {
			MatchResult notFoundRes = new MatchResult(notFoundResult);
			return invokeImpl(notFoundRes, service, requestCtx, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Void> futExc = new CompletableFuture<Void>();
			futExc.completeExceptionally(new RuntimeException("NotFound Route had an exception", e));
			return futExc;
		}
	}

	private CompletableFuture<Void> internalServerError(
			ErrorRoutes errorRoutes, Throwable exc, RequestContext requestCtx, ResponseStreamer responseCb, Object failedRoute) {
		try {
			log.error("There is three parts to this error message... request, route found, and the exception "
					+ "message.  You should\nread the exception message below  as well as the RouterRequest and RouteMeta.\n\n"
					+requestCtx.getRequest()+"\n\n"+failedRoute+".  \n\nNext, server will try to render apps 5xx page\n\n", exc);
			SupressedExceptionLog.log(exc);
			
			RouteMeta meta = errorRoutes.fetchInternalServerErrorRoute();
			MatchResult res = new MatchResult(meta);
			return invokeImpl(res, meta.getService222(), requestCtx, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Void> futExc = new CompletableFuture<Void>();
			futExc.completeExceptionally(e);
			return futExc;			
		}
	}
	
	public CompletableFuture<Void> invokeImpl(MatchResult result, Service<MethodMeta, Action> service, RequestContext requestCtx, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		ResponseProcessor processor = new ResponseProcessor(requestCtx, reverseRoutes, reverseTranslator, meta, responseCb);
		
		if(meta.getRoute().getRouteType() == RouteType.STATIC) {
			StaticRoute route = (StaticRoute) meta.getRoute();
			boolean isOnClassPath = route.getIsOnClassPath();

			RenderStaticResponse resp = new RenderStaticResponse(route.getTargetCacheLocation(), isOnClassPath);
			if(route.isFile()) {
				resp.setFilePath(route.getFileSystemPath());
			} else {
				String relativeUrl = result.getPathParams().get("resource");
				resp.setRelativeFile(route.getFileSystemPath(), relativeUrl);
			}

			return processor.renderStaticResponse(resp);
		}
		
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		if(service == null)
			throw new IllegalStateException("Bug, service should never be null at this point");
		
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeMethod(service, obj, method, meta);
		} finally {
			RequestLocalCtx.set(null);
			Current.setContext(null);
		}
		
		CompletableFuture<Void> future = response.thenApply(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}

	public Void continueProcessing(ResponseProcessor processor, Action controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof RedirectImpl) {
			processor.createFullRedirect((RedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof AjaxRedirectImpl) {
			processor.createAjaxRedirect((AjaxRedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof RenderImpl) {
			processor.createRenderResponse((RenderImpl)controllerResponse);
		} else if(controllerResponse instanceof RawRedirect) {
			processor.createRawRedirect((RawRedirect)controllerResponse);
		} else if(controllerResponse instanceof RenderContent) {
			processor.createContentResponse((RenderContent)controllerResponse);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed some code to write");
		}
		return null;
	}
	
	private CompletableFuture<Action> invokeMethod(Service<MethodMeta, Action> service, Object obj, Method m, RouteMeta meta) {
		MethodMeta methodMeta = new MethodMeta(obj, m, Current.getContext(), meta.getRoute(), meta.getBodyContentBinder());
		return service.invoke(methodMeta);
	}

	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}

	public String convertToUrl(String routeId, Map<String, String> args, boolean isValidating) {
		return reverseRoutes.convertToUrl(routeId, args, isValidating);
	}
}
