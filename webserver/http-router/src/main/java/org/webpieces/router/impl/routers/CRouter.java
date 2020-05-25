package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import com.webpieces.http2engine.api.StreamWriter;

/**
 * The request Content-Type header router routing to special Content-Type routers if they exist
 * 
 * Otherwise, it falls back to normal routing
 * 
 * @author dean
 *
 */
public class CRouter {

	private final Map<String, DContentTypeRouter> requestContentTypeToRouter;
	private final DScopedRouter allOtherRequestTypes;

	public CRouter(
		DScopedRouter allOtherContentTypesRouter, 
		Map<String, DContentTypeRouter> requestContentTypeToRouter
	) {
		this.allOtherRequestTypes = allOtherContentTypesRouter;
		this.requestContentTypeToRouter = requestContentTypeToRouter;
	}

	public CompletableFuture<StreamWriter> invokeRoute(RequestContext ctx, ProxyStreamHandle handler) {
		String relativePath = ctx.getRequest().relativePath;

		DContentTypeRouter requestTypeRouter = getRequestContentTypeToRouter().get(ctx.getRequest().domain);
		if(requestTypeRouter != null)
			return requestTypeRouter.invokeRoute(ctx, handler, relativePath);
		
		return allOtherRequestTypes.invokeRoute(ctx, handler, relativePath);
	}

	public DScopedRouter getLeftOverDomains() {
		return allOtherRequestTypes;
	}

	public Map<String, DContentTypeRouter> getRequestContentTypeToRouter() {
		return requestContentTypeToRouter;
	}

	public String build(String spacing) {
		String routes = "";
		for(Entry<String, DContentTypeRouter> entry : requestContentTypeToRouter.entrySet()) {
			routes = spacing+"Request Content-Type="+entry.getKey()+" router=\n"+entry.getValue().build(spacing);
		}
		
		routes = routes + spacing + "ALL OTHER Request Content-Types=\n"+allOtherRequestTypes.build(spacing);
		return routes;
	}

	public String buildHtml(boolean isHttps, HttpMethod method, String path, String spacing) {
		return allOtherRequestTypes.buildHtml(isHttps, method, path, spacing);
	}

}
