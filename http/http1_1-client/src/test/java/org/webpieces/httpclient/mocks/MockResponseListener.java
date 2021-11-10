package org.webpieces.httpclient.mocks;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.SocketClosedException;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class MockResponseListener implements HttpResponseListener {
	private List<Throwable> failures = new ArrayList<Throwable>();
	private boolean isClosed;

	@Override
	public XFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		return null;
	}

	@Override
	public void failure(Throwable e) {
		failures.add(e);
		if(e instanceof SocketClosedException)
			isClosed = true;
	}

	public Throwable getSingleFailure() {
		if(failures.size() != 1)
			throw new IllegalStateException("There was '"+failures.size()+"' not exactly 1 failure found");
		return failures.get(0);
	}

	public boolean isClosed() {
		return isClosed;
	}

}
