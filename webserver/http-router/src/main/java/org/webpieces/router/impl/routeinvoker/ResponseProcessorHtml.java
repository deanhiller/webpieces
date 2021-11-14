package org.webpieces.router.impl.routeinvoker;

import java.lang.reflect.Method;
import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.actions.PortRedirect;
import org.webpieces.router.impl.actions.RawRedirect;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public class ResponseProcessorHtml implements Processor {

	public ResponseProcessorHtml() {
	}

	@Override
	public XFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle) {
		if(controllerResponse instanceof RedirectImpl) {
			RedirectImpl redirect = (RedirectImpl)controllerResponse;
			return handle.sendFullRedirect(redirect.getId(), redirect.getArgs());
		} else if(controllerResponse instanceof PortRedirect) {
			PortRedirect redirect = (PortRedirect)controllerResponse;
			return handle.sendPortRedirect(redirect.getPort(), redirect.getId(), redirect.getArgs());
		} else if(controllerResponse instanceof AjaxRedirectImpl) {
			AjaxRedirectImpl redirect = (AjaxRedirectImpl)controllerResponse;
			return handle.sendAjaxRedirect(redirect.getId(), redirect.getArgs());
		} else if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse(meta, (RenderImpl)controllerResponse, handle);
		} else if(controllerResponse instanceof RawRedirect) {
			//redirects to a raw straight up url
			return createRawRedirect(meta, (RawRedirect)controllerResponse, handle);
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed some code to write="+controllerResponse);
		}
	}
	
	public XFuture<Void> createRawRedirect(MethodMeta meta, RawRedirect controllerResponse, ProxyStreamHandle handle) {
		RequestContext ctx = meta.getCtx();
		String url = controllerResponse.getUrl();
		if(url.startsWith("http")) {
			return handle.sendRedirect(new RedirectResponse(url));
		}

		RouterRequest request = ctx.getRequest();
		RedirectResponse redirectResponse = new RedirectResponse(false, request.isHttps, request.domain, request.port, url);
		return handle.sendRedirect(redirectResponse);
	}
	
	public XFuture<Void> createRenderResponse(MethodMeta meta, RenderImpl controllerResponse, ProxyStreamHandle handle) {
		RequestContext ctx = meta.getCtx();
		RouterRequest request = ctx.getRequest();

		LoadedController loadedController = meta.getLoadedController();
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
        pageArgs.put("_appContext", ctx.getApplicationContext());

		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.HTML);
		
		return handle.sendRenderHtml(resp);
	}

	
}
