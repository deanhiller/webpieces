package org.webpieces.webserver.scopes.app;

import static org.webpieces.ctx.api.HttpMethod.GET;
import static org.webpieces.ctx.api.HttpMethod.POST;
import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.router.api.routing.Routes;
import org.webpieces.router.impl.model.bldr.DomainRouteBuilder;
import org.webpieces.router.impl.model.bldr.RouteBuilder;

public class ScopesRoutes implements Routes {

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder bldr = domainRouteBldr.getAllDomainsRouteBuilder();
		bldr.addRoute(BOTH, GET , "/home",               "ScopesController.home", ScopesRouteId.HOME);

		bldr.addRoute(BOTH, GET , "/displaysession",     "ScopesController.displaySession", ScopesRouteId.DISPLAY_SESSION);
		
		bldr.addRoute(BOTH, GET , "/sessionTooLarge",    "ScopesController.sessionTooLarge", ScopesRouteId.SESSION_TOO_LARGE);
		bldr.addRoute(BOTH, GET , "/receiveTooLarge",    "ScopesController.receiveLongSession", ScopesRouteId.RECEIVE_TOO_LARGE);
		
		bldr.addRoute(BOTH, GET , "/flashmessage",      "ScopesController.flashMessage", ScopesRouteId.FLASH_MESSAGE);
		bldr.addStaticDir(BOTH, "/public/", "src/test/resources/staticRoutes/", false);
		bldr.addRoute(BOTH, GET ,   "/user/new",         "ScopesController.userAddEdit", ScopesRouteId.ADD_USER);
		bldr.addRoute(BOTH, GET ,   "/user/edit/{id}",   "ScopesController.userAddEdit", ScopesRouteId.EDIT_USER);
		bldr.addRoute(BOTH, POST,   "/user/post",        "ScopesController.postSaveUser", ScopesRouteId.POST_USER, false);
		bldr.addRoute(BOTH, GET ,   "/user/list",        "ScopesController.userList", ScopesRouteId.LIST_USER);

		//Tab state starts when TabState.start() is called in the code
		//Tab state ends when TabState.end() is called in the code
		//Tab id is either 
		//   1. in the query param ?_tab={tabid} or 
		//   2. as a url param {_tab} or 
		//   3. in the case of posts with #{form} is as a hidden field <input name="_tab" type="hidden" value="{id}"/>
//		router.addRoute(BOTH, GET , "/startwizard",      "ScopesController.userForm", ScopesRouteId.START_WIZARD);
//		router.addRoute(BOTH, GET,  "/page2form/{_tab}", "ScopesController.page2Form", ScopesRouteId.PAGE2_FORM);
//		router.addRoute(BOTH, POST, "/postpage2/{_tab}", "ScopesController.postPage2", ScopesRouteId.POST_PAGE2_FORM);
//		router.addRoute(BOTH, GET , "/page3/{_tab}",     "ScopesController.page3Form", ScopesRouteId.PAGE3_FORM);
//		//This one should result in using the hidden field from #{form} tag
//		router.addRoute(BOTH, POST, "/postpage3",        "ScopesController.postPage3Form", ScopesRouteId.POST_PAGE3_FORM);
		
		bldr.addStaticDir(BOTH, "/public/", "src/test/resources/staticRoutes/", false);
		
		bldr.setPageNotFoundRoute("/org/webpieces/webserver/basic/app/biz/BasicController.notFound");
		bldr.setInternalErrorRoute("/org/webpieces/webserver/basic/app/biz/BasicController.internalError");
	}

}
