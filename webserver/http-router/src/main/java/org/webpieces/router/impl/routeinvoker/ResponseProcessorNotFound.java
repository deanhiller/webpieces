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
import org.webpieces.router.api.PortConfigLookup;
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
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.params.ObjectToParamTranslator;
import org.webpieces.router.impl.routers.EHtmlRouter;
import org.webpieces.router.impl.routers.MatchInfo;

public class ResponseProcessorNotFound implements Processor {

	private RequestContext ctx;
	private LoadedController loadedController;
	private ResponseStreamer responseCb;
	private ReverseRoutes reverseRoutes;
	private ObjectToParamTranslator reverseTranslator;
	private RedirectFormation redirectFormation;

	public ResponseProcessorNotFound(
			RequestContext ctx, 
			ReverseRoutes reverseRoutes, 
			ObjectToParamTranslator reverseTranslator, 
			LoadedController loadedController, 
			ResponseStreamer responseCb,
			RedirectFormation redirectFormation
	) {
		this.ctx = ctx;
		this.reverseRoutes = reverseRoutes;
		this.reverseTranslator = reverseTranslator;
		this.loadedController = loadedController;
		this.responseCb = responseCb;
		this.redirectFormation = redirectFormation;
	}

	public CompletableFuture<Void> createRenderResponse(RenderImpl controllerResponse) {
		String controllerName = loadedController.getControllerInstance().getClass().getName();
		String methodName = loadedController.getControllerMethod().getName();
		
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
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.NOT_FOUND);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderHtml(resp));
	}

	public CompletableFuture<Void> createContentResponse(RenderContent r) {
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderContent(resp));
	}
	
	public CompletableFuture<Void> createFullRedirect(RedirectImpl action) {
		RouteId id = action.getId();
		Map<String, Object> args = action.getArgs();
		return createRedirect(id, args, false);
	}
	
	private CompletableFuture<Void> createRedirect(RouteId id, Map<String, Object> args, boolean isAjaxRedirect) {
		RouterRequest request = ctx.getRequest();
		Method method = loadedController.getControllerMethod();
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

		PortAndIsSecure info = redirectFormation.calculateInfo(matchInfo, null, request);
		boolean isSecure = info.isSecure();
		int port = info.getPort();
		
		RedirectResponse redirectResponse = new RedirectResponse(isAjaxRedirect, isSecure, request.domain, port, path);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRedirect(redirectResponse));
	}
	
	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse((RenderImpl) controllerResponse);
		} else if(controllerResponse instanceof RenderContent) {
			return createContentResponse((RenderContent) controllerResponse);
		} else if(controllerResponse instanceof RedirectImpl) {
			return createFullRedirect((RedirectImpl)controllerResponse);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on NotFound routes to assert the correct return types in "
					+ "ControllerLoader which is called from the RouteBuilders.  response type="+controllerResponse.getClass());
		}
	}
	
}
