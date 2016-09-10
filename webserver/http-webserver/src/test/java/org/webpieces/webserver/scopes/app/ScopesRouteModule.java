package org.webpieces.webserver.scopes.app;

import static org.webpieces.ctx.api.HttpMethod.GET;

import org.webpieces.router.api.routing.AbstractRouteModule;

public class ScopesRouteModule extends AbstractRouteModule {

	@Override
	public void configure(String currentPackage) {
		addRoute(GET , "/home",               "ScopesController.home", ScopesRouteId.HOME);

		addRoute(GET , "/displaysession",     "ScopesController.displaySession", ScopesRouteId.DISPLAY_SESSION);
		
		addRoute(GET , "/sessionTooLarge",    "ScopesController.sessionTooLarge", ScopesRouteId.SESSION_TOO_LARGE);
		addRoute(GET , "/receiveTooLarge",    "ScopesController.receiveLongSession", ScopesRouteId.RECEIVE_TOO_LARGE);
		
		//Tab state starts when TabState.start() is called in the code
		//Tab state ends when TabState.end() is called in the code
		//Tab id is either 
		//   1. in the query param ?_tab={tabid} or 
		//   2. as a url param {_tab} or 
		//   3. in the case of posts with #{form} is as a hidden field <input name="_tab" type="hidden" value="{id}"/>
//		addRoute(GET , "/startwizard",      "ScopesController.userForm", ScopesRouteId.START_WIZARD);
//		addRoute(GET,  "/page2form/{_tab}", "ScopesController.page2Form", ScopesRouteId.PAGE2_FORM);
//		addRoute(POST, "/postpage2/{_tab}", "ScopesController.postPage2", ScopesRouteId.POST_PAGE2_FORM);
//		addRoute(GET , "/page3/{_tab}",     "ScopesController.page3Form", ScopesRouteId.PAGE3_FORM);
//		//This one should result in using the hidden field from #{form} tag
//		addRoute(POST, "/postpage3",        "ScopesController.postPage3Form", ScopesRouteId.POST_PAGE3_FORM);
		
		setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
