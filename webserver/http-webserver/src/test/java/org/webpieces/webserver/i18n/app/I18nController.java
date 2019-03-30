package org.webpieces.webserver.i18n.app;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

@Singleton
public class I18nController {
	
	public Action i18nBasic() {
		String user = "Dean";
		String destination = "Italy";
		return Actions.renderThis("user", user, "destination", destination);
	}
}
