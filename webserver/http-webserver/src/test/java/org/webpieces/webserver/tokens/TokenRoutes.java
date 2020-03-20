package org.webpieces.webserver.tokens;

import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routes.Routes;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.router.api.routes.Port.BOTH;

public class TokenRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/requiredNotExist",  "TokenController.requiredNotExist", TokenRouteId.REQUIRED_TEST);
		bldr.addRoute(BOTH, GET , "/optionalNotExist",  "TokenController.optionalNotExist", TokenRouteId.OPTIONAL_TEST);
		bldr.addRoute(BOTH, GET , "/optionalNotExist2", "TokenController.optionalNotExist2", TokenRouteId.OPTIONAL_TEST2);
		bldr.addRoute(BOTH, GET , "/optionalAndNull",   "TokenController.optionalAndNull",  TokenRouteId.OPTIONAL_AND_NULL_TEST);
		bldr.addRoute(BOTH, GET , "/requiredAndNull",   "TokenController.requiredAndNull",  TokenRouteId.REQUIRED_AND_NULL_TEST);
		bldr.addRoute(BOTH, GET , "/escaping",          "TokenController.escapingTokens",   TokenRouteId.ESCAPING_ROUTE_ID);

		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
