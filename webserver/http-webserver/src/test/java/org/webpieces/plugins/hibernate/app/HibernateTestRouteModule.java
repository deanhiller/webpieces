package org.webpieces.plugins.hibernate.app;

import static org.webpieces.ctx.api.HttpMethod.*;

import org.webpieces.plugins.hibernate.TransactionFilter;
import org.webpieces.router.api.routing.AbstractRouteModule;
import org.webpieces.router.api.routing.PortType;

public class HibernateTestRouteModule extends AbstractRouteModule {

	@Override
	protected void configure(String currentPackage) {
		addRoute(POST, "/save",      "HibernateController.save", HibernateRouteId.SAVE_ENTITY, false);
		addRoute(GET , "/get/{id}",  "HibernateController.display", HibernateRouteId.DISPLAY_ENTITY);

		addRoute(POST, "/async/save",      "HibernateAsyncController.save", HibernateRouteId.ASYNC_SAVE_ENTITY, false);
		addRoute(GET , "/async/get/{id}",  "HibernateAsyncController.display", HibernateRouteId.ASYNC_DISPLAY_ENTITY);
		
		addFilter(".*", TransactionFilter.class, null, PortType.ALL_FILTER);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
