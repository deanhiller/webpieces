package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.ClientServiceConfig;
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

/**
 * Switch to FilterRoutes which is used by web and json now
 */
@Deprecated
public class JsonFilterRoutes extends FilterRoutes implements Routes {

    public JsonFilterRoutes(FilterConfig config) {
        super(config);
    }

}
