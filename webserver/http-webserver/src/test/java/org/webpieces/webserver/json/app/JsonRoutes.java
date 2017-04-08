package org.webpieces.webserver.json.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;

public class JsonRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		addContentRoute(GET , "/json/{id}",         "JsonController.jsonRequest");
		addContentRoute(POST , "/json/{id}",         "JsonController.postJson");

		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
