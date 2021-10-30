package org.webpieces.webserver.routing.app;

import com.google.common.collect.Sets;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.DefaultCorsProcessor;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.CurrentRoutes;

import java.util.List;
import java.util.Set;

public class CorsForTwoDomains implements Routes {
    public static final String DOMAIN1 = "https://someother.domain.com";
    public static final String DOMAIN_WITH_PORT = "https://other.external.com:8001";
    public static final Set<String> EXPOSED_RESPONSE_HEADERS = Sets.newHashSet("SomeResponseHeader", "AnotherHeader");

    @Override
    public void configure(DomainRouteBuilder bldr) {
        RouteBuilder rtBuilder = bldr.getAllDomainsRouteBuilder();

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.GET, "/content", "ControllerForTestOptions.getContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.PUT, "/content", "ControllerForTestOptions.putContent");

        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.POST, "/content2", "ControllerForTestOptions.postContent");
        rtBuilder.addContentRoute(Port.BOTH, HttpMethod.DELETE, "/content2", "ControllerForTestOptions.deleteContent");

        //no matter where you do this in the module, it will change the whole module to CORS
        Set<String> domains = Sets.newHashSet(DOMAIN1, DOMAIN_WITH_PORT);
        Set<String> headers = Sets.newHashSet("*");
        CurrentRoutes.setProcessCorsHook(new DefaultCorsProcessor(domains, headers, EXPOSED_RESPONSE_HEADERS, true, 86400));
    }
}
