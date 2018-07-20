package org.webpieces.plugins.fortesting;

import org.webpieces.router.api.routing.AbstractRoutes;

public class FillerRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		setPageNotFoundRoute("/org/webpieces/plugins/fortesting/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/plugins/fortesting/BasicController.internalError");
	}

}
