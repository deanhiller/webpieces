package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.PortConfig;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.impl.dto.RenderContentResponse;

public class ResponseProcessorContent implements Processor {
	
	private ResponseStreamer responseCb;

	private boolean responseSent = false;

	private RequestContext ctx;

	public ResponseProcessorContent(RequestContext ctx, ResponseStreamer responseCb) {
		this.ctx = ctx;
		this.responseCb = responseCb;
	}

	public CompletableFuture<Void> createContentResponse(RenderContent r) {
		if(responseSent)
			throw new IllegalStateException("You already sent a response.  do not call Actions.redirect or Actions.render more than once");

		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> responseCb.sendRenderContent(resp));
	}

	public CompletableFuture<Void> continueProcessing(Action controllerResponse, ResponseStreamer responseCb, PortConfig portConfig) {
		if(!(controllerResponse instanceof RenderContent)) {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on content routes to assert the correct return types");
		}
		
		return createContentResponse((RenderContent)controllerResponse);
	}
}
