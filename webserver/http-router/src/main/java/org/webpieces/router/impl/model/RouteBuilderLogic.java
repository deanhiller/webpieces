package org.webpieces.router.impl.model;

import java.io.File;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.RouteInvoker2;
import org.webpieces.router.impl.loader.ControllerLoader;

@Singleton
public class RouteBuilderLogic {
	
	//private final ReverseRoutes reverseRoutes;
	private final ControllerLoader finder;
	private final RouteInvoker2 routeInvoker;
	private final SvcProxyLogic svcProxyLogic;

	@Inject
	public RouteBuilderLogic( 
			ControllerLoader finder, 
			RouteInvoker2 routeInvoker,
			SvcProxyLogic svcProxyLogic
	) {
		this.finder = finder;
		this.routeInvoker = routeInvoker;
		this.svcProxyLogic = svcProxyLogic;
	}

	public ControllerLoader getFinder() {
		return finder;
	}

	public Charset getUrlEncoding() {
		return getConfig().getUrlEncoding();
	}

	public File getCachedCompressedDirectory() {
		return getConfig().getCachedCompressedDirectory();
	}

	public RouterConfig getConfig() {
		return getSvcProxyLogic().getConfig();
	}

	public RouteInvoker2 getRouteInvoker2() {
		return routeInvoker;
	}

	public SvcProxyLogic getSvcProxyLogic() {
		return svcProxyLogic;
	}

	public void init(ReverseRoutes reverseRoutes) {
		routeInvoker.init(reverseRoutes);		
	}

}
