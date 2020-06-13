package org.webpieces.router.impl.routeinvoker;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ControllerLoader;
import org.webpieces.util.futures.FutureHelper;

@Singleton
public class ProdRouteInvoker extends AbstractRouteInvoker {

	@Inject
	public ProdRouteInvoker(
		ControllerLoader controllerFinder,
		FutureHelper futureUtil,
		RouteInvokerStatic staticInvoker,
		BodyParsers bodyParsers
	) {
		super(controllerFinder, futureUtil, staticInvoker, bodyParsers);
	}
	

}
