package org.webpieces.router.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.RenderHtml;
import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.params.ArgumentTranslator;

public class RouteInvoker {

	private ArgumentTranslator argumentTranslator;
	
	@Inject
	public RouteInvoker(ArgumentTranslator argumentTranslator) {
		this.argumentTranslator = argumentTranslator;
	}
	
	public void invoke(ReverseRoutes reverseRoutes, 
			MatchResult result, RouterRequest req, ResponseStreamer responseCb,
			Supplier<MatchResult> notFoundRoute) {
		RouteMeta meta = result.getMeta();
		Object obj = meta.getControllerInstance();
		if(obj == null)
			throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
		Method method = meta.getMethod();

		Object[] arguments;
		try {
			arguments = argumentTranslator.createArgs(result, req);
		} catch(NotFoundException e) {
			result = notFoundRoute.get();
			meta = result.getMeta();
			obj = meta.getControllerInstance();
			if(obj == null)
				throw new IllegalStateException("Someone screwed up, as controllerInstance should not be null at this point, bug");
			method = meta.getMethod();			
			arguments = argumentTranslator.createArgs(result, req);
		}
		
		CompletableFuture<Object> response = invokeMethod(obj, method, arguments);
		
		RouteMeta finalMeta = meta;
		response.thenApply(o -> continueProcessing(reverseRoutes, req, finalMeta, o, responseCb))
			.exceptionally(e -> processException(responseCb, e));
	}

	public Object continueProcessing(ReverseRoutes reverseRoutes, RouterRequest routerRequest, RouteMeta incomingRequestMeta, Object controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof Redirect) {
			RedirectResponse httpResponse = processRedirect(reverseRoutes, routerRequest, incomingRequestMeta, (Redirect)controllerResponse);
			responseCb.sendRedirect(httpResponse);
		} else if(controllerResponse instanceof RenderHtml) {
			RenderResponse resp = renderHtml(routerRequest, incomingRequestMeta, controllerResponse);
			responseCb.sendRenderHtml(resp);
		} else {
			throw new UnsupportedOperationException("Not yet done but could "
					+ "call into the Action witht the responseCb to handle so apps can decide what to send back");
		}
		return null;
	}

	private RenderResponse renderHtml(RouterRequest routerRequest, RouteMeta incomingRequestMeta, Object controllerResponse) {
		Method method = incomingRequestMeta.getMethod();
		//in the case where the POST route was found, the controller canNOT be returning RenderHtml and should follow PRG
		//If the POST route was not found, just render the notFound page that controller sends us violating the
		//PRG pattern in this one specific case for now (until we test it with the browser to make sure back button is
		//not broken)
		if(!incomingRequestMeta.isNotFoundRoute() && HttpMethod.POST == routerRequest.method) {
			throw new IllegalReturnValueException("Controller method='"+method+"' MUST follow the PRG "
					+ "pattern(https://en.wikipedia.org/wiki/Post/Redirect/Get) so "
					+ "users don't have a poor experience using your website with the browser back button.  "
					+ "This means on a POST request, you cannot return RenderHtml object and must return Redirects");
		}
		RenderHtml renderHtml = (RenderHtml) controllerResponse;
		
		
		RenderResponse resp = new RenderResponse(renderHtml.getView(), renderHtml.getPageArgs());
		return resp;
	}

	private RedirectResponse processRedirect(ReverseRoutes reverseRoutes, RouterRequest r, RouteMeta incomingRequestMeta, Redirect action) {
		Method method = incomingRequestMeta.getMethod();
		RouteId id = action.getId();
		RouteMeta nextRequestMeta = reverseRoutes.get(id);
		
		if(nextRequestMeta == null)
			throw new IllegalReturnValueException("Route="+id+" returned from method='"+method+"' was not added in the RouterModules");

		Route route = nextRequestMeta.getRoute();
		
		Map<String, String> keysToValues = formMap(method, route.getPathParamNames(), action.getArgs());
		
		Set<String> keySet = keysToValues.keySet();
		List<String> argNames = route.getPathParamNames();
		if(keySet.size() != argNames.size()) {
			throw new IllegalReturnValueException("Method='"+method+"' returns a Redirect action with wrong number of arguments.  args="+keySet.size()+" when it should be size="+argNames.size());
		}

		String path = route.getPath();
		
		for(String name : argNames) {
			String value = keysToValues.get(name);
			if(value == null) 
				throw new IllegalArgumentException("Method='"+method+"' returns a Redirect that is missing argument key="+name+" to form the url on the redirect");
			path = path.replace("{"+name+"}", value);
		}
		
		return new RedirectResponse(r.isHttps, r.domain, path);
	}
	

	private Map<String, String> formMap(Method method, List<String> pathParamNames, List<Object> args) {
		if(pathParamNames.size() != args.size())
			throw new IllegalReturnValueException("The Redirect object returned from method='"+method+"' has the wrong number of arguments. args.size="+args.size()+" should be size="+pathParamNames.size());

		Map<String, String> nameToValue = new HashMap<>();
		for(int i = 0; i < pathParamNames.size(); i++) {
			String key = pathParamNames.get(i);
			Object obj = args.get(i);
			if(obj != null) {
				//TODO: need reverse binding here!!!!
				//Anotherwords, apps register Converters String -> Object and Object to String and we should really be
				//using that instead of toString to convert which could be different
				nameToValue.put(key, obj.toString());
			}
		}
		return nameToValue;
	}

	private Object processException(ResponseStreamer responseCb, Throwable e) {
		if(e instanceof CompletionException) {
			//unwrap the exception to deliver the 'real' exception that occurred
			e = e.getCause();
		}
		responseCb.failure(e);
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompletableFuture<Object> invokeMethod(Object obj, Method m, Object[] arguments) {
		try {
			Object retVal = m.invoke(obj, arguments);
			if(retVal instanceof CompletableFuture) {
				return (CompletableFuture) retVal;
			} else {
				return CompletableFuture.completedFuture(retVal);
			}
		} catch (Throwable e) {
			//return a completed future with the exception inside...
			CompletableFuture<Object> futExc = new CompletableFuture<Object>();
			futExc.completeExceptionally(e);
			return futExc;
		}
	}
}
