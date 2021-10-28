package org.webpieces.webserver.routing.app;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

import java.util.List;

public class CorsForAllDomains implements Routes {
    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        CurrentRoutes.setProcessCorsHook(AllDomainsRestrictHeaders.class);

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/allDomains", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/allDomains", "ControllerForTestOptions.postContent");
    }
}
