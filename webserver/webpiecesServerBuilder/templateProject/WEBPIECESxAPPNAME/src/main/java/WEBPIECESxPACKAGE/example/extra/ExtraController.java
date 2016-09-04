package WEBPIECESxPACKAGE.example.extra;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import WEBPIECESxPACKAGE.WEBPIECESxCLASSRouteId;

public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(WEBPIECESxCLASSRouteId.ANOTHER);
	}
}
