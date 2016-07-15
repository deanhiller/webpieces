package PACKAGE;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import PACKAGE.example.SomeLibrary;

public class CLASSNAMEController {
	
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
		return Actions.redirect(CLASSNAMERouteId.SOME_ROUTE);
	}
	
}
