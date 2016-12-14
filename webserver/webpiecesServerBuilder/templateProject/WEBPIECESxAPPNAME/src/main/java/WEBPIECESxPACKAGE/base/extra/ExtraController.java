package WEBPIECESxPACKAGE.base.extra;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import WEBPIECESxPACKAGE.base.routes.WEBPIECESxCLASSRouteId;

@Singleton
public class ExtraController {

	public Action relativeController() {
		return Actions.redirect(WEBPIECESxCLASSRouteId.ANOTHER);
	}
}
