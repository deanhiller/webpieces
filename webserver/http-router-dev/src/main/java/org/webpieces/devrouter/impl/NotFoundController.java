package org.webpieces.devrouter.impl;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.dto.RequestLocal;
import org.webpieces.router.api.dto.RouterRequest;

public class NotFoundController {

	public Action notFound() {
		RouterRequest request = RequestLocal.getRequest();
		String error = request.multiPartFields.get("webpiecesError").get(0);
		return Actions.renderThis("error", error);
	}
}
