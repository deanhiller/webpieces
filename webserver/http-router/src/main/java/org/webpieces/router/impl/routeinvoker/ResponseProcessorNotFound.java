package org.webpieces.router.impl.routeinvoker;

import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public class ResponseProcessorNotFound implements Processor {

	public ResponseProcessorNotFound() {
	}

	@Override
	public XFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle) {
		if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse(meta, (RenderImpl) controllerResponse, handle);
		} else if(controllerResponse instanceof RenderContent) {
			return createContentResponse(meta, (RenderContent) controllerResponse, handle);
		} else if(controllerResponse instanceof RedirectImpl) {
			RedirectImpl redirect = (RedirectImpl)controllerResponse;
			return handle.sendFullRedirect(redirect.getId(), redirect.getArgs());
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on NotFound routes to assert the correct return types in "
					+ "ControllerLoader which is called from the RouteBuilders.  response type="+controllerResponse.getClass());
		}
	}
	
	public XFuture<Void> createRenderResponse(MethodMeta meta, RenderImpl controllerResponse, ProxyStreamHandle handle) {
		LoadedController loadedController = meta.getLoadedController();
		String controllerName = loadedController.getControllerInstance().getClass().getName();
		String methodName = loadedController.getControllerMethod().getName();
		
		String relativeOrAbsolutePath = controllerResponse.getRelativeOrAbsolutePath();
		if(relativeOrAbsolutePath == null) {
			relativeOrAbsolutePath = methodName+".html";
		}

		Map<String, Object> pageArgs = controllerResponse.getPageArgs();

		RequestContext ctx = meta.getCtx();
        // Add context as a page arg:
        pageArgs.put("_context", ctx);
        pageArgs.put("_session", ctx.getSession());
        pageArgs.put("_flash", ctx.getFlash());
        pageArgs.put("_appContext", ctx.getApplicationContext());

		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.NOT_FOUND);
		
		return handle.sendRenderHtml(resp);
	}

	public XFuture<Void> createContentResponse(MethodMeta meta, RenderContent r, ProxyStreamHandle handle) {
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return handle.sendRenderContent(resp);
	}
	

	
}
