package org.webpieces.router.api.routing;

public abstract class ScopedRouteModule extends AbstractRouteModule {
	
	@Override
	public final void configure(Router router) {
		String scope = getScope();
		if(scope != null && scope.length() > 0) {
			this.router = router.getScopedRouter(scope);
		} else {
			this.router = router;
		}
		configure();
	}
	
	/**
	 * Scoped routers have preference and within the scope of say "/backend" all routes are matching 
	 * what is after the "/backend" part of the url
	 * @return
	 */
	protected abstract String getScope();

	@Override
	protected abstract void configure();

}
