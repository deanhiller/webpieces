package org.webpieces.microsvc.server.api;

import org.webpieces.microsvc.server.impl.filters.HeaderToRequestStateFilter;
import org.webpieces.microsvc.server.impl.filters.MDCFilter;
import org.webpieces.microsvc.server.impl.filters.RequestIdFilter;
import org.webpieces.microsvc.server.impl.filters.TokenSharingFilter;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.FilterPortType;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

public class JsonFilterRoutes implements Routes {

    private FilterConfig config;

    public JsonFilterRoutes(FilterConfig config) {
        this.config = config;
    }

    @Override
    public void configure(DomainRouteBuilder domainRouteBldr) {

        RouteBuilder builder = domainRouteBldr.getAllDomainsRouteBuilder();

        String regex = config.getPackageRegEx();

        builder.setInternalErrorRoute("../impl/controllers/JsonErrorNotFoundController.internalError");
        builder.setPageNotFoundRoute("../impl/controllers/JsonErrorNotFoundController.notFound");

        if(!config.isEntryPoint()) {
            builder.addPackageFilter(regex, TokenSharingFilter.class, config.getCheckHeaderName(), FilterPortType.ALL_FILTER, 160);
        }
        builder.addPackageFilter(regex, RequestIdFilter.class, config.getSvcName(), FilterPortType.ALL_FILTER, 140);
        builder.addPackageFilter(regex, HeaderToRequestStateFilter.class, config.getHeaders(), FilterPortType.ALL_FILTER, 120);
        builder.addPackageFilter(regex, MDCFilter.class, config.getHeaders(), FilterPortType.ALL_FILTER, 100);

        //complicate port as-is so we will do this in Tray for now until we can port this one too ->
        //builder.addPackageFilter(regex, MetricsFilter.class, null, FilterPortType.ALL_FILTER, 80);

        if(config.isEnableHealthCheckEndpoint())
            builder.addContentRoute(BOTH, GET, "/health", "../impl/controllers/HealthController.health");
    }

}
