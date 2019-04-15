package org.webpieces.router.impl.routeinvoker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.impl.actions.RenderImpl;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.View;

public class ResponseProcessorAppError implements Processor {

	private RequestContext ctx;
	private ProcessorInfo matchedMeta;
	private ResponseStreamer responseCb;

	private boolean responseSent = false;

	public ResponseProcessorAppError(RequestContext ctx, ProcessorInfo meta, ResponseStreamer responseCb) {
		this.ctx = ctx;
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

	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb, PortConfig portConfig) {
		if(!(controllerResponse instanceof RenderImpl)) {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on error routes to assert the correct return types in ControllerLoader which is called from the RouteBuilders");
		}
		return createRenderResponse((RenderImpl) controllerResponse);
	}
	
}
