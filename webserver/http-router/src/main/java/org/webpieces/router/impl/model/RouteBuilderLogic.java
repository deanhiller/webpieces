package org.webpieces.router.impl.model;

import java.io.File;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.util.futures.FutureHelper;

@Singleton
public class RouteBuilderLogic {
	
	//private final ReverseRoutes reverseRoutes;
	private final ControllerLoader finder;
	private final RouteInvoker routeInvoker;
	private final SvcProxyLogic svcProxyLogic;

	@Inject
	public RouteBuilderLogic( 
			ControllerLoader finder, 
			RouteInvoker routeInvoker,
			SvcProxyLogic svcProxyLogic,
			BodyContentBinderChecker binderChecker
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

	public RouteInvoker getRouteInvoker2() {
		return routeInvoker;
	}

	public SvcProxyLogic getSvcProxyLogic() {
		return svcProxyLogic;
	}

	public void init(ReverseRoutes reverseRoutes) {
		routeInvoker.init(reverseRoutes);		
	}

}
