package org.webpieces.router.impl.routers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import com.webpieces.http2engine.api.StreamWriter;

/**
 * Routes based on domain or if no domain delegates to allOtherDomainsRouter
 */
public class BRouter {
	private static final Logger log = LoggerFactory.getLogger(BRouter.class);

	private final Map<String, CRouter> domainToRouter;
	private final CRouter allOtherDomainsRouter;
	private CRouter backendRouter;

	public BRouter(
		CRouter allOtherDomainsRouter, 
		CRouter backendRouter, //ONLY enabled IF configured
		Map<String, CRouter> domainToRouter
	) {
		this.allOtherDomainsRouter = allOtherDomainsRouter;
		this.backendRouter = backendRouter;
		this.domainToRouter = domainToRouter;
	}

	public CompletableFuture<StreamWriter> invokeRoute(RequestContext ctx, ProxyStreamHandle handler) {
		if(ctx.getRequest().isBackendRequest) {
			return backendRouter.invokeRoute(ctx, handler);
		}

		CRouter specificDomainRouter = getDomainToRouter().get(ctx.getRequest().domain);
		if(specificDomainRouter != null)
			return specificDomainRouter.invokeRoute(ctx, handler);
		
		return allOtherDomainsRouter.invokeRoute(ctx, handler);
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