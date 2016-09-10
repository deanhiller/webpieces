package org.webpieces.router.impl;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.util.filters.Service;

public class NotFoundInfo {

	private MatchResult result;
	private RouterRequest req;
	private Service<MethodMeta, Action> service;

	public NotFoundInfo(MatchResult result,  Service<MethodMeta, Action> service, RouterRequest req) {
		this.result = result;
		this.service = service;
		this.req = req;
	}

	public MatchResult getResult() {
		return result;
	}

	public Service<MethodMeta, Action> getService() {
		return service;
	}

	public RouterRequest getReq() {
		return req;
	}
	
}
