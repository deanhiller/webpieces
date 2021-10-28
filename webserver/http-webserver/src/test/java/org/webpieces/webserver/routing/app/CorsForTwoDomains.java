package org.webpieces.webserver.routing.app;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

import java.util.List;

public class CorsForTwoDomains implements Routes {
    public static final String DOMAIN1 = "https://someother.domain.com";
    public static final String DOMAIN_WITH_PORT = "https://other.external.com:8001";

    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        CurrentRoutes.setProcessCorsHook(AllHeadersRestrictDomains.class);

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/content", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.PUT, "/content", "ControllerForTestOptions.putContent");

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/content2", "ControllerForTestOptions.postContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.DELETE, "/content2", "ControllerForTestOptions.deleteContent");
    }
}
