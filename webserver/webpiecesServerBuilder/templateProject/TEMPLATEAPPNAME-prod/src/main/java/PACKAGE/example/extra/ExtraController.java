package PACKAGE.example.extra;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import PACKAGE.CLASSNAMERouteId;

public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(CLASSNAMERouteId.ANOTHER);
	}
}
