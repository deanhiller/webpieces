package org.webpieces.router.impl.ctx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Flash;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.ctx.api.Validation;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderHtmlImpl;
import org.webpieces.router.impl.params.ObjectToStringTranslator;

public class ResponseProcessor {
	
	private ReverseRoutes reverseRoutes;
	private RouteMeta matchedMeta;
	private ObjectToStringTranslator reverseTranslator;
	private RequestContext ctx;
	private ResponseStreamer responseCb;
	private CookieTranslator cookieFactory;
	private boolean responseSent = false;

	public ResponseProcessor(RequestContext ctx, ReverseRoutes reverseRoutes, 
			ObjectToStringTranslator reverseTranslator, RouteMeta meta, ResponseStreamer responseCb,
			CookieTranslator cookieFactory) {
		this.ctx = ctx;
		this.reverseRoutes = reverseRoutes;
		this.reverseTranslator = reverseTranslator;
		this.matchedMeta = meta;
		this.responseCb = responseCb;
		this.cookieFactory = cookieFactory;
	}

	public RedirectResponse createFullRedirect(RedirectImpl action) {
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
		
		List<RouterCookie> cookies = createCookies();
		
		RedirectResponse redirectResponse = new RedirectResponse(request.isHttps, request.domain, path, cookies);
		
		responseCb.sendRedirect(redirectResponse);
		
		return redirectResponse;
	}

	private List<RouterCookie> createCookies() {
		List<RouterCookie> cookies = new ArrayList<>();
		Flash flash = ctx.getFlash();
		cookieFactory.addScopeToCookieIfExist(cookies, flash);
		Validation validation = ctx.getValidation();
		cookieFactory.addScopeToCookieIfExist(cookies, validation);
		return cookies;
	}

	public RenderResponse createRenderResponse(RenderHtmlImpl controllerResponse) {
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
		
		List<RouterCookie> cookies = createCookies();
		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, controllerResponse.getPageArgs(), matchedMeta.getRoute().getRouteType(), cookies);
		
		boolean wasSet = Current.isContextSet();
		if(!wasSet)
			Current.setContext(ctx); //Allow html tags to use the contexts
		try {
			responseCb.sendRenderHtml(resp);
		} finally {
			if(!wasSet) //then reset
				Current.setContext(null);
		}
		
		return resp;
	}
	
}
