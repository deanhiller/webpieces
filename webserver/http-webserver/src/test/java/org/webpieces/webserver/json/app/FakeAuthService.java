package org.webpieces.webserver.json.app;

import org.webpieces.util.futures.XFuture;

public class FakeAuthService {

	public XFuture<Boolean> authenticate(String username) {
		return XFuture.completedFuture(true);
	}

	public void saveRequest(SearchRequest request) {
	}
}
