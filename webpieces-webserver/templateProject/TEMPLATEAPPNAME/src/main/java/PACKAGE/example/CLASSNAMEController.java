package PACKAGE.example;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import PACKAGE.CLASSNAMERouteId;

public class CLASSNAMEController {

	public Action redirect(String id) {
		return Actions.redirect(CLASSNAMERouteId.RENDER_PAGE);
	}
	
	public Action myMethod() {
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis("hhhh");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
}
