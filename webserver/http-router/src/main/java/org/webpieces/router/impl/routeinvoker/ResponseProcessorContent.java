package org.webpieces.router.impl.routeinvoker;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.impl.actions.AjaxRedirectImpl;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

@Singleton
public class ResponseProcessorContent implements Processor {

	@Inject
	public ResponseProcessorContent() {
	}

	@Override
	public CompletableFuture<Void> continueProcessing(MethodMeta meta, Action controllerResponse, ProxyStreamHandle handle) {
		if(controllerResponse instanceof AjaxRedirectImpl) {
			AjaxRedirectImpl redirect = (AjaxRedirectImpl) controllerResponse;
			return handle.sendAjaxRedirect(redirect.getId(), redirect.getArgs());
		} else if(!(controllerResponse instanceof RenderContent)) {
			throw new UnsupportedOperationException("One of the your RouteFilters in your App has a bug " +
					"in that it calls Actions.redirect or Actions.render which is only for html pages");
		}
		
		return createContentResponse(meta, (RenderContent)controllerResponse, handle);
	}
	
	public CompletableFuture<Void> createContentResponse(MethodMeta meta, RenderContent r, ProxyStreamHandle handle) {
		RequestContext ctx = meta.getCtx();
		RenderContentResponse resp = new RenderContentResponse(r.getContent(), r.getStatusCode(), r.getReason(), r.getMimeType());
		return ContextWrap.wrap(ctx, () -> handle.sendRenderContent(resp));
	}

}
