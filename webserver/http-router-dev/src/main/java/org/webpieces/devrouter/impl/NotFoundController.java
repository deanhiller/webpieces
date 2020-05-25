package org.webpieces.devrouter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.impl.RoutingHolder;
import org.webpieces.router.impl.routers.BRouter;
import org.webpieces.router.impl.routers.CRouter;

@Singleton
public class NotFoundController {

	public static final String ORIGINAL_REQUEST = "__originalRequest";
	private final RoutingHolder routingHolder;
	
	@Inject
	public NotFoundController(RoutingHolder routingHolder) {
		super();
		this.routingHolder = routingHolder;
	}

	public Action notFound() {
		RouterRequest request = Current.request();
		String error = request.getSingleMultipart("webpiecesError");
		String url = request.getSingleMultipart("url");
		
		if(url.contains("?")) {
			url += "&webpiecesShowPage=true";
		} else {
			url += "?webpiecesShowPage=true";
		}

		Collection<CRouter> routers = new ArrayList<>();
		CRouter router;
		BRouter domainRouter = routingHolder.getDomainRouter();

		if(request.isBackendRequest) {
			router = domainRouter.getBackendRouter();
		} else {
			router = domainRouter.getLeftOverDomains();
			
			for(CRouter oneRouter : domainRouter.getDomainToRouter().values()) {
				routers.add(oneRouter);
			}
		}
		
		RouterRequest req = (RouterRequest) request.requestState.get(ORIGINAL_REQUEST);
		//This is a pain but dynamically build up the html
		String routeHtml = build(req.relativePath, router);
		
		
		List<String> paths = new ArrayList<>();
		if(req.isHttps) {
			paths.add(req.method+" :https : "+req.relativePath);
		} else {
			paths.add(req.method+" :https : "+req.relativePath);
			paths.add(req.method+" :http : "+req.relativePath);
		}
		
		return Actions.renderThis("domains", routers, "paths", paths, "routeHtml", routeHtml, "error", error, "url", url);
	}

	private String build(String path, CRouter mainRoutes) {
		return mainRoutes.buildHtml(path, " ");
	}
}
