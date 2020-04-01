package org.webpieces.router.impl.routeinvoker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.loader.LoadedController;

public class ResponseProcessorAppError implements Processor {

	private RequestContext ctx;
	private LoadedController loadedController;
	private ResponseStreamer responseCb;

	public ResponseProcessorAppError(RequestContext ctx, LoadedController loadedController, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.loadedController = loadedController;
		this.responseCb = responseCb;
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
		RenderResponse resp = new RenderResponse(view, pageArgs, RouteType.INTERNAL_SERVER_ERROR);
		
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderHtml(resp));
	}

	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb) {
		if(!(controllerResponse instanceof RenderImpl)) {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on error routes to assert the correct return types in ControllerLoader which is called from the RouteBuilders");
		}
		return createRenderResponse((RenderImpl) controllerResponse);
	}
	
}
