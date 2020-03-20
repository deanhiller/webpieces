package org.webpieces.webserver.tags.field;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;

import javax.inject.Singleton;

@Singleton
public class FieldTagController {

	public Action customFieldTag() {
		return Actions.renderThis("user", "Dean");
	}
}
