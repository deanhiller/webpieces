package org.webpieces.plugins.backend;

import org.webpieces.router.api.routing.AbstractRoutes;

public class FillerRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
