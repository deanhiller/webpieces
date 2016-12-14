package WEBPIECESxPACKAGE.base.extra;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import WEBPIECESxPACKAGE.base.example.SomeLibrary;
import WEBPIECESxPACKAGE.base.routes.WEBPIECESxCLASSRouteId;

@Singleton
public class WEBPIECESxCLASSController {
	
	@Inject
	private SomeLibrary someLibrary;
	
	public Action myMethod() {
		someLibrary.doSomething();
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis(
				"user", "Dean Hiller",
				"id", 500,
				"otherKey", "key");
	}
	
	public Action anotherMethod() {
		return Actions.redirect(WEBPIECESxCLASSRouteId.SOME_ROUTE);
	}
	
}
