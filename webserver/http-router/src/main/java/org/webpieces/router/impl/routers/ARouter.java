package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

@Singleton
public class ARouter {

	private BRouter domainRouter;

	@Inject
	public ARouter() {
		
	}
	
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb) {
		return domainRouter.invokeRoute(ctx, responseCb);
	}

	public void printAllRoutes() {
		domainRouter.printRoutes();
	}

	public void setDomainRouter(BRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
