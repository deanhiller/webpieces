package PACKAGE;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class CLASSNAMEController {

	public Action redirect() {
		return Actions.redirect(CLASSNAMERouteId.RENDER_PAGE);
	}
	
	public Action render() {
		return Actions.renderThis("hhhh");
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
}
