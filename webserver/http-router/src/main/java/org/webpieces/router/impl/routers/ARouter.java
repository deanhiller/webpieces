package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;

import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

@Singleton
public class ARouter {

	private BRouter domainRouter;

	@Inject
	public ARouter() {
		
	}
	
	public RouterStreamRef invoke(RequestContext ctx, ProxyStreamHandle handler) {
		return domainRouter.invokeRoute(ctx, handler);
	}

	public void printAllRoutes() {
		domainRouter.printRoutes();
	}

	public void setDomainRouter(BRouter domainRouter) {
		this.domainRouter = domainRouter;
	}

}
