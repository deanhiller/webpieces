package WEBPIECESxPACKAGE.base.json;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;

import org.webpieces.router.api.routing.AbstractRoutes;

public class JsonRoutes extends AbstractRoutes {

	@Override
	public void configure() {
		
		addContentRoute(GET , "/json/read",         "JsonController.readOnly");

		addContentRoute(GET , "/json/{id}",         "JsonController.jsonRequest");
		addContentRoute(POST , "/json/{id}",        "JsonController.postJson");

		addContentRoute(GET , "/json/async/{id}",   "JsonController.asyncJsonRequest");
		addContentRoute(POST, "/json/async/{id}",   "JsonController.postAsyncJson");

		addContentRoute(GET , "/json/throw/{id}",        "JsonController.throwNotFound");

	}

}
