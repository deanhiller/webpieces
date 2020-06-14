package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

@Singleton
public class ResponseProcessorContent implements Processor {

	@Inject
	public ResponseProcessorContent() {
	}

	@Override
	public CompletableFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle) {
		if(!(controllerResponse instanceof RenderContent)) {
			throw new UnsupportedOperationException("Bug, a webpieces developer must have missed writing a "
					+ "precondition check on content routes to assert the correct return types");
		}
		
		return createContentResponse(meta, (RenderContent)controllerResponse, handle);
	}
	
	public CompletableFuture<Void> createContentResponse(MethodMeta meta, RenderContent r, ProxyStreamHandle handle) {
		RequestContext ctx = meta.getCtx();
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> handle.sendRenderContent(resp));
	}

}
