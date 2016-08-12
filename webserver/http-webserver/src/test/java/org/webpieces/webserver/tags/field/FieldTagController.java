package org.webpieces.webserver.tags.field;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

public class FieldTagController {

	public Action customFieldTag() {
		return Actions.renderThis("user", "Dean");
	}
}
