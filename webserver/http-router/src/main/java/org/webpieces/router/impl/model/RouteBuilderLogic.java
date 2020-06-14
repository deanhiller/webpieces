package org.webpieces.router.impl.model;

import java.io.File;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.router.impl.routeinvoker.ResponseProcessorContent;
import org.webpieces.router.impl.routeinvoker.ResponseProcessorHtml;
import org.webpieces.router.impl.routeinvoker.RouteInvoker;
import org.webpieces.router.impl.routeinvoker.ServiceInvoker;
import org.webpieces.util.futures.FutureHelper;

@Singleton
public class RouteBuilderLogic {
	
	//private final ReverseRoutes reverseRoutes;
	private final ControllerLoader finder;
	private final RouteInvoker routeInvoker;
	private final SvcProxyLogic svcProxyLogic;
	private FutureHelper futureUtil;
	private ResponseProcessorContent responseProcessorContent;
	private ResponseProcessorHtml responseProcessorHtml;
	private BodyParsers bodyParsers;
	private ServiceInvoker serviceInvoker;

	@Inject
	public RouteBuilderLogic( 
			ControllerLoader finder, 
			RouteInvoker routeInvoker,
			SvcProxyLogic svcProxyLogic,
			BodyContentBinderChecker binderChecker,
			FutureHelper futureUtil,
			ResponseProcessorContent responseProcessorContent,
			ResponseProcessorHtml responseProcessorHtml,
			BodyParsers bodyParsers,
			ServiceInvoker serviceInvoker
	) {
		this.finder = finder;
		this.routeInvoker = routeInvoker;
		this.svcProxyLogic = svcProxyLogic;
		this.futureUtil = futureUtil;
		this.responseProcessorContent = responseProcessorContent;
		this.responseProcessorHtml = responseProcessorHtml;
		this.bodyParsers = bodyParsers;
		this.serviceInvoker = serviceInvoker;
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
		serviceInvoker.init(reverseRoutes);		
	}

	public FutureHelper getFutureUtil() {
		return futureUtil;
	}

	public ResponseProcessorContent getResponseProcessorContent() {
		return responseProcessorContent;
	}

	public ResponseProcessorHtml getResponseProcessorHtml() {
		return responseProcessorHtml;
	}

	public BodyParsers getBodyParsers() {
		return bodyParsers;
	}

	public ServiceInvoker getServiceInvoker() {
		return serviceInvoker;
	}

}
