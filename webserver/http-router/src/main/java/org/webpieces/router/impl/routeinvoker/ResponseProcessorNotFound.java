package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.routers.EHtmlRouter;
import org.webpieces.router.impl.routers.MatchInfo;

public class ResponseProcessorNotFound implements Processor {

	private RequestContext ctx;
	private ProcessorInfo matchedMeta;
	private ResponseStreamer responseCb;
	private ReverseRoutes reverseRoutes;
	private ObjectToParamTranslator reverseTranslator;
	
	private boolean responseSent = false;

	public ResponseProcessorNotFound(RequestContext ctx, ReverseRoutes reverseRoutes, 
			ObjectToParamTranslator reverseTranslator, ProcessorInfo meta, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.reverseRoutes = reverseRoutes;
		this.reverseTranslator = reverseTranslator;
		this.matchedMeta = meta;
		this.responseCb = responseCb;
	}

	public CompletableFuture<Void> createRenderResponse(RenderImpl controllerResponse) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");
		responseSent = true;
		
		String controllerName = matchedMeta.getControllerInstance().getClass().getName();
		String methodName = matchedMeta.getMethod().getName();
		
		String relativeOrAbsolutePath = controllerResponse.getRelativeOrAbsolutePath();
		if(relativeOrAbsolutePath == null) {
			relativeOrAbsolutePath = methodName+".html";
		}

		Map<String, Object> pageArgs = controllerResponse.getPageArgs();

        // Add context as a page arg:
        pageArgs.put("_context", ctx);
        pageArgs.put("_session", ctx.getSession());
        pageArgs.put("_flash", ctx.getFlash());

		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, pageArgs, matchedMeta.getRouteType());
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderHtml(resp));
	}

	public CompletableFuture<Void> createContentResponse(RenderContent r) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");

		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderContent(resp));
	}
	
	public CompletableFuture<Void> createFullRedirect(RedirectImpl action, PortConfig portConfig) {
		RouteId id = action.getId();
		Map<String, Object> args = action.getArgs();
		return createRedirect(id, args, false, portConfig);
	}
	
	private CompletableFuture<Void> createRedirect(RouteId id, Map<String, Object> args, boolean isAjaxRedirect, PortConfig portConfig) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");
		responseSent = true;
		RouterRequest request = ctx.getRequest();
		Method method = matchedMeta.getMethod();
		EHtmlRouter nextRequestMeta = reverseRoutes.get(id);
		if(nextRequestMeta == null)
			throw new IllegalReturnValueException("Route="+id+" returned from method='"+method+"' was not added in the RouterModules");
		
		MatchInfo matchInfo = nextRequestMeta.getMatchInfo();

		if(!matchInfo.matchesMethod(HttpMethod.GET))
			throw new IllegalReturnValueException("method='"+method+"' is trying to redirect to routeid="+id+" but that route is not a GET method route and must be");

		Map<String, String> keysToValues = reverseTranslator.formMap(method, matchInfo.getPathParamNames(), args);

		Set<String> keySet = keysToValues.keySet();
		List<String> argNames = matchInfo.getPathParamNames();
		if(keySet.size() != argNames.size()) {
			throw new IllegalReturnValueException("Method='"+method+"' returns a Redirect action with wrong number of arguments.  args="+keySet.size()+" when it should be size="+argNames.size());
		}

		String path = matchInfo.getFullPath();
		
		for(String name : argNames) {
			String value = keysToValues.get(name);
			if(value == null) 
				throw new IllegalArgumentException("Method='"+method+"' returns a Redirect that is missing argument key="+name+" to form the url on the redirect");
			path = path.replace("{"+name+"}", value);
		}

		boolean isHttpsOnly = matchInfo.getExposedPorts() == Port.HTTPS;
		
		//if the request is https, stay in https as everything is accessible on https
		//if the request is http, then convert to https IF new route is secure
		boolean isSecure = request.isHttps || isHttpsOnly;
		int port = request.port;
		//if need to change port to https port, this is how we do it...
		if(!request.isHttps && isHttpsOnly)
			port = portConfig.getHttpsPort();
		
		RedirectResponse redirectResponse = new RedirectResponse(isAjaxRedirect, isSecure, request.domain, port, path);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRedirect(redirectResponse));
	}
	
	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb, PortConfig portConfig) {
		if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse((RenderImpl) controllerResponse);
		} else if(controllerResponse instanceof RenderContent) {
			return createContentResponse((RenderContent) controllerResponse);
		} else if(controllerResponse instanceof RedirectImpl) {
			return createFullRedirect((RedirectImpl)controllerResponse, portConfig);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on NotFound routes to assert the correct return types in "
					+ "ControllerLoader which is called from the RouteBuilders.  response type="+controllerResponse.getClass());
		}
	}
	
}
