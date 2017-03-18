package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.FlashSub;
import org.webpieces.ctx.api.Messages;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Session;
import org.webpieces.ctx.api.Validation;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.exceptions.BadRequestException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.ctx.FlashImpl;
import org.webpieces.router.impl.ctx.RequestLocalCtx;
import org.webpieces.router.impl.ctx.ResponseProcessor;
import org.webpieces.router.impl.ctx.SessionImpl;
import org.webpieces.router.impl.ctx.ValidationImpl;
import org.webpieces.router.impl.model.MatchResult;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.params.ObjectTranslator;
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
	private CookieTranslator cookieTranslator;
	private ObjectTranslator objectTranslator;
	
	@Inject
	public RouteInvoker(
		ObjectToParamTranslator reverseTranslator,
		RouterConfig config,
		CookieTranslator cookieTranslator,
		ObjectTranslator objectTranslator
	) {
		this.reverseTranslator = reverseTranslator;
		this.config = config;
		this.cookieTranslator = cookieTranslator;
		this.objectTranslator = objectTranslator;
	}

	public void invoke(MatchResult result, RouterRequest routerRequest, ResponseStreamer responseCb, ErrorRoutes errorRoutes) {
		Session session = (Session) cookieTranslator.translateCookieToScope(routerRequest, new SessionImpl(objectTranslator));
		FlashSub flash = (FlashSub) cookieTranslator.translateCookieToScope(routerRequest, new FlashImpl(objectTranslator));
		Validation validation = (Validation) cookieTranslator.translateCookieToScope(routerRequest, new ValidationImpl(objectTranslator));
		RequestContext requestCtx = new RequestContext(validation, flash, session, routerRequest, result.getPathParams());
		
		invokeImpl(result, routerRequest, responseCb, errorRoutes, requestCtx);
	}
	
	public void invokeImpl(
			MatchResult result, RouterRequest req, ResponseStreamer responseCb, ErrorRoutes errorRoutes, RequestContext requestCtx) {
		try {
			//We convert all exceptions from invokeAsync into CompletableFuture..
			CompletableFuture<Void> future = invokeAsync(result, requestCtx, responseCb, errorRoutes);
			future.exceptionally(e -> processException(responseCb, requestCtx, e, errorRoutes, result.getMeta()));
		} catch(Throwable e) {
			responseCb.failureRenderingInternalServerErrorPage(e);
		}
	}
	
	private Void processException(ResponseStreamer responseCb, RequestContext requestCtx, Throwable e, ErrorRoutes errorRoutes, RouteMeta meta) {
		if(e instanceof CompletionException) {
			//unwrap the exception to deliver the 'real' exception that occurred
			e = e.getCause();
		}
		
		if(e == null || e instanceof NotFoundException) {
			NotFoundException exc = (NotFoundException) e;
			NotFoundInfo notFoundInfo = errorRoutes.fetchNotfoundRoute(exc);
			RouteMeta notFoundResult = notFoundInfo.getResult();
			RouterRequest overridenRequest = notFoundInfo.getReq();
			RequestContext overridenCtx = new RequestContext(requestCtx.getValidation(), (FlashSub) requestCtx.getFlash(), requestCtx.getSession(), overridenRequest, new HashMap<>());
			
			//http 404...(unless an exception happens in calling this code and that then goes to 500)
			CompletableFuture<Void> future = notFound(notFoundResult, notFoundInfo.getService(), exc, overridenCtx, responseCb);
			//If not found fails with sync or async exception, we processException and wrap in new Runtime to process as 500 next
			future.exceptionally(exception -> processException(responseCb, overridenCtx, new RuntimeException("notFound page failed", exception), errorRoutes, notFoundResult));
			return null;
		}

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
	
	public CompletableFuture<Void> invokeAsync(
		MatchResult result, RequestContext reqCtx, ResponseStreamer responseCb, ErrorRoutes notFoundRoute) {
		try {
			//This makes us consistent with other NotFoundExceptions and without the cost of 
			//throwing an exception and filling in stack trace...
			//We could convert the exc. to FastException and override method so stack is not filled in but that
			//can get very annoying
			RouteMeta meta = result.getMeta();
			Route route = meta.getRoute();
			RouteType routeType = route.getRouteType();
			if(routeType == RouteType.NOT_FOUND) {
				processException(responseCb, reqCtx, null, notFoundRoute, null);
				//This is a special case....check the NotFound tests
				return CompletableFuture.completedFuture(null);
			}

			return invokeImpl(result, meta.getService222(), reqCtx, responseCb);
		} catch (Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Void> futExc = new CompletableFuture<Void>();
			futExc.completeExceptionally(e);
			return futExc;
		}
	}
	
	private CompletableFuture<Void> notFound(RouteMeta notFoundResult, Service<MethodMeta, Action> service, NotFoundException exc, RequestContext requestCtx, ResponseStreamer responseCb) {
		try {
			MatchResult notFoundRes = new MatchResult(notFoundResult);
			return invokeImpl(notFoundRes, service, requestCtx, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Void> futExc = new CompletableFuture<Void>();
			futExc.completeExceptionally(e);
			return futExc;			
		}
	}

	private CompletableFuture<Void> internalServerError(
			ErrorRoutes errorRoutes, Throwable exc, RequestContext requestCtx, ResponseStreamer responseCb, RouteMeta failedRoute) {
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
		
		RouterRequest req = requestCtx.getRequest();
		Messages messages = new Messages(meta.getI18nBundleName(), "webpieces");
		requestCtx.setMessages(messages);

		if(config.isTokenCheckOn() && meta.getRoute().isCheckSecureToken()) {
			String token = requestCtx.getSession().get(SessionImpl.SECURE_TOKEN_KEY);
			String formToken = req.multiPartFields.get(RequestContext.SECURE_TOKEN_FORM_NAME);
			if(formToken == null)
				throw new BadRequestException("missing form token(or route added without setting checkToken variable to false)"
						+ "...someone posting form without getting it first(hacker or otherwise) OR "
						+ "you are not using the #{form}# tag or the #{secureToken}# tag to secure your forms");
			else if(!formToken.equals(token))
				throw new BadRequestException("bad form token...someone posting form with invalid token(hacker or otherwise)");
		}

		RequestLocalCtx.set(processor);
		Current.setContext(requestCtx);
		CompletableFuture<Action> response;
		try {
			response = invokeMethod(service, obj, method);
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
		} else if(controllerResponse instanceof RenderImpl) {
			processor.createRenderResponse((RenderImpl)controllerResponse);
		} else if(controllerResponse instanceof RawRedirect) {
			processor.createRawRedirect((RawRedirect)controllerResponse);
		} else {
			throw new UnsupportedOperationException("Not yet done but could "
					+ "call into the Action witht the responseCb to handle so apps can decide what to send back");
		}
		return null;
	}
	
	private CompletableFuture<Action> invokeMethod(Service<MethodMeta, Action> service, Object obj, Method m) {
		MethodMeta meta = new MethodMeta(obj, m, Current.getContext());
		return service.invoke(meta);
	}

	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}

	public String convertToUrl(String routeId, Map<String, String> args) {
		return reverseRoutes.convertToUrl(routeId, args);
	}
}
