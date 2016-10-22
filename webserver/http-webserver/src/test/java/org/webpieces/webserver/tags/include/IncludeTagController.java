package org.webpieces.webserver.tags.include;

import javax.inject.Singleton;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

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
