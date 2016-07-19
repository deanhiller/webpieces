package org.webpieces.router.impl;

import org.webpieces.router.api.dto.RouterRequest;

public class NotFoundInfo {

	private MatchResult result;
	private RouterRequest req;

	public NotFoundInfo(MatchResult result, RouterRequest req) {
		this.result = result;
		this.req = req;
	}

	public MatchResult getResult() {
		return result;
	}

	public RouterRequest getReq() {
		return req;
	}
	
}
