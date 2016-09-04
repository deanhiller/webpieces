package org.webpieces.webserver.i18n.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class I18nRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		addRoute(GET , "/i18nBasic",         "I18nController.i18nBasic", I18nRouteId.I18N_BASIC);
		
		setPageNotFoundRoute("../../basic/biz/BasicController.notFound");
		setInternalErrorRoute("../../basic/biz/BasicController.internalError");
	}

}