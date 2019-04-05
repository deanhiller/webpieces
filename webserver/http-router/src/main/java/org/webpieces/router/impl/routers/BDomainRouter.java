package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * Routes based on domain or if no domain delegates to allOtherDomainsRouter
 */
public class BDomainRouter {
	private static final Logger log = LoggerFactory.getLogger(BDomainRouter.class);

	private final Map<String, CRouter> domainToRouter;
	private final CRouter allOtherDomainsRouter;

	public BDomainRouter(CRouter allOtherDomainsRouter, Map<String, CRouter> domainToRouter) {
		this.allOtherDomainsRouter = allOtherDomainsRouter;
		this.domainToRouter = domainToRouter;
	}

	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb) {
		String relativePath = ctx.getRequest().relativePath;
		CRouter specificDomainRouter = getDomainToRouter().get(ctx.getRequest().domain);
		if(specificDomainRouter != null)
			return specificDomainRouter.invokeRoute(ctx, responseCb, relativePath);
		
		return allOtherDomainsRouter.invokeRoute(ctx, responseCb, relativePath);
	}

	public CRouter getLeftOverDomains() {
		return allOtherDomainsRouter;
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