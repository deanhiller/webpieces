package org.webpieces.devrouter.impl;

import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

@Singleton
public class NotFoundController {

	public Action notFound() {
		RouterRequest request = Current.request();
		String error = request.multiPartFields.get("webpiecesError");
		String url = request.multiPartFields.get("url");
		
		if(url.contains("?")) {
			url += "&webpiecesShowPage=true";
		} else {
			url += "?webpiecesShowPage=true";
		}
		return Actions.renderThis("error", error, "url", url);
	}
}
