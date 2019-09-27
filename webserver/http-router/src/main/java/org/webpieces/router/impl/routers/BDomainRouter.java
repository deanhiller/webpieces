package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes based on domain or if no domain delegates to allOtherDomainsRouter
 */
public class BDomainRouter {
	private static final Logger log = LoggerFactory.getLogger(BDomainRouter.class);

	private final Map<String, CRouter> domainToRouter;
	private final CRouter allOtherDomainsRouter;
	private CRouter backendRouter;

	public BDomainRouter(
		CRouter allOtherDomainsRouter, 
		CRouter backendRouter, //ONLY enabled IF configured
		Map<String, CRouter> domainToRouter
	) {
		this.allOtherDomainsRouter = allOtherDomainsRouter;
		this.backendRouter = backendRouter;
		this.domainToRouter = domainToRouter;
	}

	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb) {
		String relativePath = ctx.getRequest().relativePath;

		if(ctx.getRequest().isBackendRequest) {
			return backendRouter.invokeRoute(ctx, responseCb, relativePath);
		}

		CRouter specificDomainRouter = getDomainToRouter().get(ctx.getRequest().domain);
		if(specificDomainRouter != null)
			return specificDomainRouter.invokeRoute(ctx, responseCb, relativePath);
		
		return allOtherDomainsRouter.invokeRoute(ctx, responseCb, relativePath);
	}

	public CRouter getLeftOverDomains() {
		return allOtherDomainsRouter;
	}

	
	public CRouter getBackendRouter() {
		return backendRouter;
	}

	public Map<String, CRouter> getDomainToRouter() {
		return domainToRouter;
	}

	public void printRoutes() {
		String spacing = "   ";
		for(Entry<String, CRouter> entry : domainToRouter.entrySet()) {
			log.warn("Domain="+entry.getKey()+" router=\n"+entry.getValue().build(spacing));
		}
		
		log.warn("ALL OTHER DOMAINS=\n"+allOtherDomainsRouter.build(spacing));
	}
}