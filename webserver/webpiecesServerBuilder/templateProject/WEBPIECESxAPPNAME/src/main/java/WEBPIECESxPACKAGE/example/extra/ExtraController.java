package WEBPIECESxPACKAGE.example.extra;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import WEBPIECESxPACKAGE.WEBPIECESxCLASSRouteId;

@Singleton
public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(WEBPIECESxCLASSRouteId.ANOTHER);
	}
}
