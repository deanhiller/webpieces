package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

@Singleton
public class AMasterRouter {

	private BDomainRouter domainRouter;

	@Inject
	public AMasterRouter() {
		
	}
	
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb) {
		return domainRouter.invokeRoute(ctx, responseCb);
	}

	public void printAllRoutes() {
		domainRouter.printRoutes();
	}

	public void setDomainRouter(BDomainRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
