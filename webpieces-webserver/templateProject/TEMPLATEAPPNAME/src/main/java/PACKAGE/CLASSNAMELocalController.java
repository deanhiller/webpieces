package PACKAGE;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class CLASSNAMELocalController {

	public Action someMethod() {
		return Actions.redirect(CLASSNAMERouteId.RENDER_PAGE);
	}
}
