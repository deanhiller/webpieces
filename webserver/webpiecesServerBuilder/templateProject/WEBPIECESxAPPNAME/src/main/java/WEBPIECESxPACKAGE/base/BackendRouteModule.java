package WEBPIECESxPACKAGE.base;

import org.webpieces.router.api.routing.ScopedRouteModule;

public class BackendRouteModule extends ScopedRouteModule {

	/**
	 * All routes will be matches after "/backend" so they are all grouped into a logical area of
	 * your webapp.
	 */
	@Override
	protected String getScope() {
		return "/backend";
	}

	@Override
	protected void configure() {
	}

}
