package org.webpieces.webserver.json;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.webserver.json.app.FakeAuthService;
import org.webpieces.webserver.json.app.SearchRequest;

public class MockAuthService extends FakeAuthService {

	private List<XFuture<Boolean>> futures = new ArrayList<XFuture<Boolean>>();
	private SearchRequest request;

	@Override
	public XFuture<Boolean> authenticate(String username) {
		return futures.remove(0);
	}

	public void addValueToReturn(XFuture<Boolean> future) {
		this.futures.add(future);
	}
	
	public void saveRequest(SearchRequest request) {
		this.request = request;
	}
	
	public SearchRequest getCachedRequest() {
		return request;
	}
}
