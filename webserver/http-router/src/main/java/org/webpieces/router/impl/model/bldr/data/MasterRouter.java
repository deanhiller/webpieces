package org.webpieces.router.impl.model.bldr.data;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;

@Singleton
public class MasterRouter {

	private DomainRouter domainRouter;

	@Inject
	public MasterRouter() {
		
	}
	
	public CompletableFuture<Void> invoke(RequestContext ctx, ResponseStreamer responseCb) {
		return domainRouter.invokeRoute(ctx, responseCb);
	}

	public void printAllRoutes() {
		domainRouter.printRoutes();
	}

	public void setDomainRouter(DomainRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
