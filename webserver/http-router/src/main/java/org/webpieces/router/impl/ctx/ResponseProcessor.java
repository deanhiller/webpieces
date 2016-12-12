package org.webpieces.router.impl.ctx;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.params.ObjectToParamTranslator;

public class ResponseProcessor {
	
	private ReverseRoutes reverseRoutes;
	private RouteMeta matchedMeta;
	private ObjectToParamTranslator reverseTranslator;
	private RequestContext ctx;
	private ResponseStreamer responseCb;

	private boolean responseSent = false;

	public ResponseProcessor(RequestContext ctx, ReverseRoutes reverseRoutes, 
			ObjectToParamTranslator reverseTranslator, RouteMeta meta, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.reverseRoutes = reverseRoutes;
		this.reverseTranslator = reverseTranslator;
		this.matchedMeta = meta;
		this.responseCb = responseCb;
	}

	public void createRawRedirect(RawRedirect controllerResponse) {
		String url = controllerResponse.getUrl();
		if(url.startsWith("http")) {
			wrapFunctionInContext(s -> responseCb.sendRedirect(new RedirectResponse(url)));
			return;
		}

		RouterRequest request = ctx.getRequest();
		RedirectResponse redirectResponse = new RedirectResponse(request.isHttps, request.domain, url);
		wrapFunctionInContext(s -> responseCb.sendRedirect(redirectResponse));
	}
	
	public void createFullRedirect(RedirectImpl action) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");
		responseSent = true;
		RouterRequest request = ctx.getRequest();
		Method method = matchedMeta.getMethod();
		RouteId id = action.getId();
		RouteMeta nextRequestMeta = reverseRoutes.get(id);
		
		if(nextRequestMeta == null)
			throw new IllegalReturnValueException("Route="+id+" returned from method='"+method+"' was not added in the RouterModules");
		else if(!nextRequestMeta.getRoute().matchesMethod(HttpMethod.GET))
			throw new IllegalReturnValueException("method='"+method+"' is trying to redirect to routeid="+id+" but that route is not a GET method route and must be");

		Route route = nextRequestMeta.getRoute();
		
		Map<String, String> keysToValues = reverseTranslator.formMap(method, route.getPathParamNames(), action.getArgs());
		
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
		
		RedirectResponse redirectResponse = new RedirectResponse(request.isHttps, request.domain, path);
		
		wrapFunctionInContext(s -> responseCb.sendRedirect(redirectResponse));
	}

	public void createRenderResponse(RenderImpl controllerResponse) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");
		responseSent = true;
		
		RouterRequest request = ctx.getRequest();

		Method method = matchedMeta.getMethod();
		//in the case where the POST route was found, the controller canNOT be returning RenderHtml and should follow PRG
		//If the POST route was not found, just render the notFound page that controller sends us violating the
		//PRG pattern in this one specific case for now (until we test it with the browser to make sure back button is
		//not broken)
		if(matchedMeta.getRoute().getRouteType() == RouteType.BASIC && HttpMethod.POST == request.method) {
			throw new IllegalReturnValueException("Controller method='"+method+"' MUST follow the PRG "
					+ "pattern(https://en.wikipedia.org/wiki/Post/Redirect/Get) so "
					+ "users don't have a poor experience using your website with the browser back button.  "
					+ "This means on a POST request, you cannot return RenderHtml object and must return Redirects");
		}

		String controllerName = matchedMeta.getControllerInstance().getClass().getName();
		String methodName = matchedMeta.getMethod().getName();
		
		String relativeOrAbsolutePath = controllerResponse.getRelativeOrAbsolutePath();
		if(relativeOrAbsolutePath == null) {
			relativeOrAbsolutePath = methodName+".html";
		}

		Map<String, Object> pageArgs = controllerResponse.getPageArgs();

        // Add context as a page arg:
        pageArgs.put("_context", ctx);

		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, pageArgs, matchedMeta.getRoute().getRouteType());
		
		wrapFunctionInContext(s -> responseCb.sendRenderHtml(resp));
	}

	private void wrapFunctionInContext(Consumer<Void> function) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			function.accept(null);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}

	public void failureRenderingInternalServerErrorPage(Throwable e) {
		wrapFunctionInContext(s -> responseCb.failureRenderingInternalServerErrorPage(e));
	}

	public CompletableFuture<Void> renderStaticResponse(RenderStaticResponse renderStatic) {
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			return responseCb.sendRenderStatic(renderStatic);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
	}
	
}
