package PACKAGE;

import org.webpieces.router.api.dto.HttpMethod;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;

public class CLASSNAMERoutes implements RouteModule {

	//This is just ONE 
	@Override
	public void configure(Router router, String currentPackage) {

		router.addRoute(HttpMethod.GET, "/redirect/{id}", "CLASSNAMEController.redirect", CLASSNAMERouteId.REDIRECT_PAGE);

		router.addRoute(HttpMethod.GET, "/render", "CLASSNAMEController.render", CLASSNAMERouteId.RENDER_PAGE);
		
		router.setNotFoundRoute("CLASSNAMEController.notFound");
	}

}
