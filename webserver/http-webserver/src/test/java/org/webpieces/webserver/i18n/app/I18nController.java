package org.webpieces.webserver.i18n.app;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class I18nController {
	
	public Action i18nBasic() {
		String user = "Dean";
		String destination = "Italy";
		return Actions.renderThis("user", user, "destination", destination);
	}
}
