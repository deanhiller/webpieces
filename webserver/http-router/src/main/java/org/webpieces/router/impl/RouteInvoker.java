package org.webpieces.router.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RequestLocal;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderHtmlImpl;
import org.webpieces.router.impl.ctx.LocalContext;
import org.webpieces.router.impl.ctx.ResponseProcessor;
import org.webpieces.router.impl.params.ArgumentTranslator;
import org.webpieces.router.impl.params.ObjectToStringTranslator;
import org.webpieces.router.impl.params.Validation;

public class RouteInvoker {

	private static final Logger log = LoggerFactory.getLogger(RouteInvoker.class);
	private ArgumentTranslator argumentTranslator;
	//initialized in init() method and re-initialized in dev mode from that same method..
	private ReverseRoutes reverseRoutes;
	private ObjectToStringTranslator reverseTranslator;
	
	@Inject
	public RouteInvoker(ArgumentTranslator argumentTranslator, ObjectToStringTranslator translator) {
		this.argumentTranslator = argumentTranslator;
		this.reverseTranslator = translator;
	}

	public void invoke(
			MatchResult result, RouterRequest req, ResponseStreamer responseCb, ErrorRoutes errorRoutes) {
		//We convert all exceptions from invokeAsync into CompletableFuture..
		CompletableFuture<Object> future = invokeAsync(result, req, responseCb, errorRoutes);
		future.exceptionally(e -> processException(responseCb, req, e, errorRoutes, result.getMeta()));
	}
	
	private Object processException(ResponseStreamer responseCb, RouterRequest req, Throwable e, ErrorRoutes errorRoutes, RouteMeta meta) {
		if(e instanceof CompletionException) {
			//unwrap the exception to deliver the 'real' exception that occurred
			e = e.getCause();
		}
		
		if(e == null || e instanceof NotFoundException) {
			NotFoundException exc = (NotFoundException) e;
			NotFoundInfo notFoundInfo = errorRoutes.fetchNotfoundRoute(exc);
			MatchResult notFoundResult = notFoundInfo.getResult();
			RouterRequest overridenRequest = notFoundInfo.getReq();
			//http 404...(unless an exception happens in calling this code and that then goes to 500)
			CompletableFuture<Object> future = notFound(notFoundResult, exc, overridenRequest, responseCb);
			//If not found fails with sync or async exception, we processException and wrap in new Runtime to process as 500 next
			future.exceptionally(exception -> processException(responseCb, overridenRequest, new RuntimeException("notFound page failed", exception), errorRoutes, notFoundResult.getMeta()));
			return null;
		}

		//If this fails, then the users 5xx page is messed up and we then render our own 5xx page
		CompletableFuture<Object> future = internalServerError(errorRoutes, e, req, responseCb, meta);
		future.exceptionally(finalExc -> finalFailure(responseCb, finalExc));
		
		return null;
	}
	
	public Object finalFailure(ResponseStreamer responseCb, Throwable e) {
		log.error("This is a final(secondary failure) trying to render the Internal Server Error Route", e); 
		responseCb.failureRenderingInternalServerErrorPage(e);
		return null;
	}
	
	public CompletableFuture<Object> invokeAsync(
		MatchResult result, RouterRequest req, ResponseStreamer responseCb, ErrorRoutes notFoundRoute) {
		try {
			//This makes us consistent with other NotFoundExceptions and without the cost of 
			//throwing an exception and filling in stack trace...
			//We could convert the exc. to FastException and override method so stack is not filled in but that
			//can get very annoying
			if(result.getMeta().getRoute().getRouteType() == RouteType.NOT_FOUND) {
				processException(responseCb, req, null, notFoundRoute, null);
				//This is a special case....check the NotFound tests
				return CompletableFuture.completedFuture(null);
			}

			return invokeImpl(result, req, responseCb);
		} catch (Throwable e) {
			log.info("msg", e);
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Object> futExc = new CompletableFuture<Object>();
			futExc.completeExceptionally(e);
			return futExc;
		}
	}
	
	private CompletableFuture<Object> notFound(MatchResult notFoundResult, NotFoundException exc, RouterRequest req, ResponseStreamer responseCb) {
		try {
			return invokeImpl(notFoundResult, req, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Object> futExc = new CompletableFuture<Object>();
			futExc.completeExceptionally(e);
			return futExc;			
		}
	}

	private CompletableFuture<Object> internalServerError(
			ErrorRoutes errorRoutes, Throwable exc, RouterRequest req, ResponseStreamer responseCb, RouteMeta failedRoute) {
		try {
			log.error("There is three parts to this error message... request, route found, and the exception "
					+ "message.  You should\nread the exception message below  as well as the RouterRequest and RouteMeta.\n\n"+req+"\n\n"+failedRoute+".  \n\nNext, server will try to render apps 5xx page\n\n", exc);
			MatchResult result = errorRoutes.fetchInternalServerErrorRoute();
			return invokeImpl(result, req, responseCb);
		} catch(Throwable e) {
			//http 500...
			//return a completed future with the exception inside...
			CompletableFuture<Object> futExc = new CompletableFuture<Object>();
			futExc.completeExceptionally(e);
			return futExc;			
		}
	}
	
	public CompletableFuture<Object> invokeImpl(MatchResult result, RouterRequest req, ResponseStreamer responseCb) {
		RouteMeta meta = result.getMeta();
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		Validation validator = new Validation();
		Object[] arguments = argumentTranslator.createArgs(result, req, validator);

		ResponseProcessor processor = new ResponseProcessor(reverseRoutes, reverseTranslator, req, meta);
		
		//ThreadLocals...
		LocalContext.setResponseProcessor(processor);
		Validation.set(validator);
		RequestLocal.setRequest(req);
		
		CompletableFuture<Object> response;
		try {
			response = invokeMethod(obj, method, arguments);
		} finally {
			//Clear ThreadLocals...
			LocalContext.setResponseProcessor(null);
			Validation.set(null);
			RequestLocal.setRequest(null);
		}
		
		CompletableFuture<Object> future = response.thenApply(resp -> continueProcessing(processor, resp, responseCb));
		return future;
	}

	public Object continueProcessing(ResponseProcessor processor, Object controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof RedirectResponse) {
			responseCb.sendRedirect((RedirectResponse)controllerResponse);
		} else if(controllerResponse instanceof RenderResponse) {
			responseCb.sendRenderHtml((RenderResponse)controllerResponse);
		} else if(controllerResponse instanceof RedirectImpl) {
			RedirectResponse httpResponse = processor.createFullRedirect((RedirectImpl)controllerResponse);
			responseCb.sendRedirect(httpResponse);
		} else if(controllerResponse instanceof RenderHtmlImpl) {
			RenderResponse resp = processor.createRenderResponse((RenderHtmlImpl)controllerResponse);
			responseCb.sendRenderHtml(resp);
		} else {
			throw new UnsupportedOperationException("Not yet done but could "
					+ "call into the Action witht the responseCb to handle so apps can decide what to send back");
		}
		return null;
	}
	
	private CompletableFuture<Object> invokeMethod(Object obj, Method m, Object[] arguments) {
		try {
			return invokeMethodImpl(obj, m, arguments);
		} catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			} else {
				throw new InvokeException(e);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new InvokeException(e);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompletableFuture<Object> invokeMethodImpl(Object obj, Method m, Object[] arguments) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Object retVal = m.invoke(obj, arguments);
		if(retVal instanceof CompletableFuture) {
			return (CompletableFuture) retVal;
		} else {
			return CompletableFuture.completedFuture(retVal);
		}
	}

	public void init(ReverseRoutes reverseRoutes) {
		this.reverseRoutes = reverseRoutes;
	}

	public String convertToUrl(String routeId, Map<String, String> args) {
		return reverseRoutes.convertToUrl(routeId, args);
	}
}
