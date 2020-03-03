package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.UrlInfo;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.PortRedirect;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;

public class ResponseProcessorHtml implements Processor {

	private RequestContext ctx;
	private ReverseRoutes reverseRoutes;
	private LoadedController loadedController;
	private ResponseStreamer responseCb;

	public ResponseProcessorHtml(
			RequestContext ctx, 
			ReverseRoutes reverseRoutes,
			LoadedController loadedController, 
			ResponseStreamer responseCb 
	) {
		this.ctx = ctx;
		this.reverseRoutes = reverseRoutes;
		this.loadedController = loadedController;
		this.responseCb = responseCb;
	}

	public CompletableFuture<Void> createRawRedirect(RawRedirect controllerResponse) {
		String url = controllerResponse.getUrl();
		if(url.startsWith("http")) {
			return ContextWrap.wrap(ctx, () -> responseCb.sendRedirect(new RedirectResponse(url)));
		}

		RouterRequest request = ctx.getRequest();
		RedirectResponse redirectResponse = new RedirectResponse(false, request.isHttps, request.domain, request.port, url);
		return ContextWrap.wrap(ctx, () -> responseCb.sendRedirect(redirectResponse));
	}
	

	
	public CompletableFuture<Void> createAjaxRedirect(AjaxRedirectImpl action) {
		RouteId id = action.getId();
		Map<String, Object> args = action.getArgs();
		return createRedirect(null, id, args, true);
	}
	
	public CompletableFuture<Void> createFullRedirect(RedirectImpl action) {
		RouteId id = action.getId();
		Map<String, Object> args = action.getArgs();
		return createRedirect(null, id, args, false);
	}

	public CompletableFuture<Void> createPortRedirect(PortRedirect action) {
		RouteId id = action.getId();
		Map<String, Object> args = action.getArgs();
		HttpPort port = action.getPort();
		return createRedirect(port, id, args, false);
	}
	
	private CompletableFuture<Void> createRedirect(
			HttpPort requestedPort, RouteId id, Map<String, Object> args, boolean isAjaxRedirect) {
		RouterRequest request = ctx.getRequest();
		Method method = loadedController.getControllerMethod();
		
		UrlInfo urlInfo = reverseRoutes.routeToUrl(id, method, args, request, requestedPort);
		boolean isSecure = urlInfo.isSecure();
		int port = urlInfo.getPort();
		String path = urlInfo.getPath();
			
		RedirectResponse redirectResponse = new RedirectResponse(isAjaxRedirect, isSecure, request.domain, port, path);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRedirect(redirectResponse));
	}

	public CompletableFuture<Void> createRenderResponse(RenderImpl controllerResponse) {
		RouterRequest request = ctx.getRequest();

		Method method = loadedController.getControllerMethod();
		//in the case where the POST route was found, the controller canNOT be returning RenderHtml and should follow PRG
		//If the POST route was not found, just render the notFound page that controller sends us violating the
		//PRG pattern in this one specific case for now (until we test it with the browser to make sure back button is
		//not broken)
		if(HttpMethod.POST == request.method) {
			throw new IllegalReturnValueException("Controller method='"+method+"' MUST follow the PRG "
					+ "pattern(https://en.wikipedia.org/wiki/Post/Redirect/Get) so "
					+ "users don't have a poor experience using your website with the browser back button.  "
					+ "This means on a POST request, you cannot return RenderHtml object and must return Redirects");
		}

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
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.HTML);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderHtml(resp));
	}

	public CompletableFuture<Void> createContentResponse(RenderContent r) {
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderContent(resp));
	}

	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb) {
		if(controllerResponse instanceof RedirectImpl) {
			return createFullRedirect((RedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof PortRedirect) {
			return createPortRedirect((PortRedirect)controllerResponse);
		} else if(controllerResponse instanceof AjaxRedirectImpl) {
			return createAjaxRedirect((AjaxRedirectImpl)controllerResponse);
		} else if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse((RenderImpl)controllerResponse);
		} else if(controllerResponse instanceof RawRedirect) {
			//redirects to a raw straight up url
			return createRawRedirect((RawRedirect)controllerResponse);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed some code to write");
		}
	}
	
}
