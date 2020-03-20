package org.webpieces.webserver.tags.include;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

import javax.inject.Singleton;

@Singleton
public class IncludeTagController {

	public Action customTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public Action renderPageArgsTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public Action renderTagArgsTag() {
		return Actions.renderThis("user", "Dean Hiller");
	}
}
