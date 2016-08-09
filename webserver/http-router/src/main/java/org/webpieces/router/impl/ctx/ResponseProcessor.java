package org.webpieces.router.impl.ctx;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouteType;
import org.webpieces.router.api.dto.RouterRequest;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.Route;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderHtmlImpl;
import org.webpieces.router.impl.params.ObjectToStringTranslator;

public class ResponseProcessor {
	
	private ReverseRoutes reverseRoutes;
	private RouterRequest request;
	private RouteMeta matchedMeta;
	private ObjectToStringTranslator reverseTranslator;

	public ResponseProcessor(ReverseRoutes reverseRoutes, ObjectToStringTranslator reverseTranslator, RouterRequest req, RouteMeta meta) {
		this.reverseRoutes = reverseRoutes;
		this.reverseTranslator = reverseTranslator;
		this.request = req;
		this.matchedMeta = meta;
	}

	public RedirectResponse createFullRedirect(RedirectImpl action) {
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
		
		return new RedirectResponse(request.isHttps, request.domain, path);
	}

	public RenderResponse createRenderResponse(RenderHtmlImpl controllerResponse) {
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
		
		View view = controllerResponse.getView();
		if(controllerResponse.getView() == null) {
			String controllerName = matchedMeta.getControllerInstance().getClass().getName();
			String methodName = matchedMeta.getMethod().getName();
			view = new View(controllerName, methodName);
		}
		
		RenderResponse resp = new RenderResponse(view, controllerResponse.getPageArgs(), matchedMeta.getRoute().getRouteType());
		return resp;
	}
}
