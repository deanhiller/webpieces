package org.webpieces.router.impl.routing;

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
public class DomainRouter {
	private static final Logger log = LoggerFactory.getLogger(DomainRouter.class);

	private final Map<String, Router> domainToRouter;
	private final Router allOtherDomainsRouter;

	public DomainRouter(Router allOtherDomainsRouter, Map<String, Router> domainToRouter) {
		this.allOtherDomainsRouter = allOtherDomainsRouter;
		this.domainToRouter = domainToRouter;
	}

	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb) {
		String relativePath = ctx.getRequest().relativePath;
		Router specificDomainRouter = getDomainToRouter().get(ctx.getRequest().domain);
		if(specificDomainRouter != null)
			return specificDomainRouter.invokeRoute(ctx, responseCb, relativePath);
		
		return allOtherDomainsRouter.invokeRoute(ctx, responseCb, relativePath);
	}

	public Router getLeftOverDomains() {
		return allOtherDomainsRouter;
	}

	public Map<String, Router> getDomainToRouter() {
		return domainToRouter;
	}

	
	public void printRoutes() {
		String spacing = "   ";
		for(Entry<String, Router> entry : domainToRouter.entrySet()) {
			log.warn("Domain="+entry.getKey()+" router=\n"+entry.getValue().build(spacing));
		}
		
		log.warn("ALL OTHER DOMAINS=\n"+allOtherDomainsRouter.build(spacing));
	}
}