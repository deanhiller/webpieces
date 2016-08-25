package org.webpieces.webserver.scopes.app;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.Session;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class ScopesController {

	public Action home() {
		Session session = Current.session();
		session.put("age", 30);
		session.put("boolean", true);
		session.put("name", "Dean");
		
		return Actions.renderThis();
	}
	
	public Action displaySession() {
		Session session = Current.session();
		Integer age = session.get("age", Integer.class);
		Boolean b = session.get("boolean", Boolean.class);
		String name = session.get("name");
		
		return Actions.renderThis("age", age, "result", b, "name", name);
	}
//	addRoute(GET , "/home",               "ScopesController.home", ScopesRouteId.HOME);
//
//	addRoute(GET , "/displaysession",     "ScopesController.displaySession", ScopesRouteId.DISPLAY_SESSION);
//	
//	//Tab state starts when TabState.start() is called in the code
//	//Tab state ends when TabState.end() is called in the code
//	//Tab id is either 
//	//   1. in the query param ?_tab={tabid} or 
//	//   2. as a url param {_tab} or 
//	//   3. in the case of posts with #{form} is as a hidden field <input name="_tab" type="hidden" value="{id}"/>
//	addRoute(GET , "/startwizard",      "ScopesController.userForm", ScopesRouteId.START_WIZARD);
//	addRoute(GET,  "/page2form/{_tab}", "ScopesController.page2Form", ScopesRouteId.PAGE2_FORM);
//	addRoute(POST, "/postpage2/{_tab}", "ScopesController.postPage2", ScopesRouteId.POST_PAGE2_FORM);
//	addRoute(GET , "/page3/{_tab}",     "ScopesController.page3Form", ScopesRouteId.PAGE3_FORM);
//	//This one should result in using the hidden field from #{form} tag
//	addRoute(POST, "/postpage3",        "ScopesController.postPage3Form", ScopesRouteId.POST_PAGE3_FORM);	

}
