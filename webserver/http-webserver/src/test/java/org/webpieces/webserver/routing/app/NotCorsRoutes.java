package org.webpieces.webserver.routing.app;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

public class NotCorsRoutes implements Routes {
    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/content", "ControllerForTestOptions.postContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.DELETE, "/content", "ControllerForTestOptions.deleteContent");

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/content2", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.PUT, "/content2", "ControllerForTestOptions.putContent");

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/content3", "ControllerForTestOptions.getContent");
    }
}
