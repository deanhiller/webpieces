package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

public class CRouter {

	private final Map<String, DRouter> requestContentTypeToRouter;
	private final DRouter allOtherRequestTypes;

	public CRouter(
		DRouter allOtherContentTypesRouter, 
		Map<String, DRouter> requestContentTypeToRouter
	) {
		this.allOtherRequestTypes = allOtherContentTypesRouter;
		this.requestContentTypeToRouter = requestContentTypeToRouter;
	}

	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb) {
		String relativePath = ctx.getRequest().relativePath;

		DRouter requestTypeRouter = getRequestContentTypeToRouter().get(ctx.getRequest().domain);
		if(requestTypeRouter != null)
			return requestTypeRouter.invokeRoute(ctx, responseCb, relativePath);
		
		return allOtherRequestTypes.invokeRoute(ctx, responseCb, relativePath);
	}

	public DRouter getLeftOverDomains() {
		return allOtherRequestTypes;
	}

	public Map<String, DRouter> getRequestContentTypeToRouter() {
		return requestContentTypeToRouter;
	}

	public String build(String spacing) {
		String routes = "";
		for(Entry<String, DRouter> entry : requestContentTypeToRouter.entrySet()) {
			routes = spacing+"Request Content-Type="+entry.getKey()+" router=\n"+entry.getValue().build(spacing);
		}
		
		routes = routes + spacing + "ALL OTHER Request Content-Types=\n"+allOtherRequestTypes.build(spacing);
		return routes;
	}

	public String buildHtml(String spacing) {
		return allOtherRequestTypes.buildHtml(spacing);
	}

}
