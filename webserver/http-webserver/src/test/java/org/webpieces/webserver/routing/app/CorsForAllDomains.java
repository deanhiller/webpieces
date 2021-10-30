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
    public static final Set<String> ALLOWED_REQUEST_HEADERS = Sets.newHashSet("Authorization", "Content-Type");

    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        Set<String> domains = Sets.newHashSet("*");
        Set<String> exposedResponseHeaders = Sets.newHashSet();
        CurrentRoutes.setProcessCorsHook(new DefaultCorsProcessor(domains, ALLOWED_REQUEST_HEADERS, exposedResponseHeaders, false, 86400));

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/allDomains", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/allDomains", "ControllerForTestOptions.postContent");
    }
}
