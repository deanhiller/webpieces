package org.webpieces.webserver.routing.app;

import com.google.common.collect.Sets;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.DefaultCorsProcessor;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CorsForAllDomains implements Routes {
    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        Set<String> domains = Sets.newHashSet("*");
        Set<String> headers = Sets.newHashSet("Authorization", "Content-Type");
        CurrentRoutes.setProcessCorsHook(new DefaultCorsProcessor(domains, headers));

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/allDomains", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/allDomains", "ControllerForTestOptions.postContent");
    }
}
