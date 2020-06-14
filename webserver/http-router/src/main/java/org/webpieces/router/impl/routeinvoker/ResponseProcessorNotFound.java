package org.webpieces.router.impl.routeinvoker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.impl.actions.RedirectImpl;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

public class ResponseProcessorNotFound implements Processor {

	private RequestContext ctx;
	private LoadedController loadedController;
	private ProxyStreamHandle responseCb;

	public ResponseProcessorNotFound(
			InvokeInfo info
	) {
		ctx = info.getRequestCtx();
		loadedController = info.getLoadedController();
		responseCb = info.getHandler();
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
        pageArgs.put("_appContext", ctx.getApplicationContext());

		View view = new View(controllerName, methodName, relativeOrAbsolutePath);
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.NOT_FOUND);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderHtml(resp));
	}

	public CompletableFuture<Void> createContentResponse(RenderContent r) {
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderContent(resp));
	}
	
	public CompletableFuture<Void> continueProcessing(Action controllerResponse) {
		if(controllerResponse instanceof RenderImpl) {
			return createRenderResponse((RenderImpl) controllerResponse);
		} else if(controllerResponse instanceof RenderContent) {
			return createContentResponse((RenderContent) controllerResponse);
		} else if(controllerResponse instanceof RedirectImpl) {
			RedirectImpl redirect = (RedirectImpl)controllerResponse;
			return responseCb.sendFullRedirect(redirect.getId(), redirect.getArgs());
		} else {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on NotFound routes to assert the correct return types in "
					+ "ControllerLoader which is called from the RouteBuilders.  response type="+controllerResponse.getClass());
		}
	}
	
}
