package PACKAGE.example;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import PACKAGE.CLASSNAMERouteId;

public class CLASSNAMEController {

	public Action redirect(String id) {
		return Actions.redirect(CLASSNAMERouteId.RENDER_PAGE);
	}
	
	public Action myMethod() {
		return Actions.renderThis("hhhh");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
}
