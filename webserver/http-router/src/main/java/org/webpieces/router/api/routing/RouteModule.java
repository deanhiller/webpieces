package org.webpieces.router.api.routing;

public interface RouteModule {

	public void configure(Router router);
	
	public String getI18nBundleName();

}
