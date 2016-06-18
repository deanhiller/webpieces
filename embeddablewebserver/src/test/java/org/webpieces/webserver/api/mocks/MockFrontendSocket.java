package org.webpieces.webserver.api.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.nio.api.channels.Channel;

public class MockFrontendSocket implements FrontendSocket {

	private List<HttpPayload> payloads = new ArrayList<>();
	private boolean isClosed;

	@Override
	public CompletableFuture<FrontendSocket> close() {
		isClosed = true;
		return null;
	}

	@Override
	public CompletableFuture<FrontendSocket> write(HttpPayload payload) {
		this.payloads .add(payload);
		return null;
	}

	@Override
	public Channel getUnderlyingChannel() {
		return null;
	}

	public boolean isClosed() {
		return isClosed;
	}
}
