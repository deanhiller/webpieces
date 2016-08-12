package org.webpieces.webserver.tags.include;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

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
