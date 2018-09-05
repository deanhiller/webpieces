package org.webpieces.webserver.tokens;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRoutes;

public class TokenRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addRoute(GET , "/requiredNotExist",  "TokenController.requiredNotExist", TokenRouteId.REQUIRED_TEST);
		addRoute(GET , "/optionalNotExist",  "TokenController.optionalNotExist", TokenRouteId.OPTIONAL_TEST);
		addRoute(GET , "/optionalAndNull",   "TokenController.optionalAndNull",  TokenRouteId.OPTIONAL_AND_NULL_TEST);
		addRoute(GET , "/requiredAndNull",   "TokenController.requiredAndNull",  TokenRouteId.REQUIRED_AND_NULL_TEST);

		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
