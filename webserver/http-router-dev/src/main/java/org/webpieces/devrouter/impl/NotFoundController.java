package org.webpieces.devrouter.impl;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.ctx.Request;
import org.webpieces.router.api.dto.RouterRequest;

public class NotFoundController {

	public Action notFound() {
		RouterRequest request = Request.request();
		String error = request.multiPartFields.get("webpiecesError").get(0);
		return Actions.renderThis("error", error);
	}
}
