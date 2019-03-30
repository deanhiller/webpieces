package org.webpieces.webserver.tags.field;

import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

@Singleton
public class FieldTagController {

	public Action customFieldTag() {
		return Actions.renderThis("user", "Dean");
	}
}
