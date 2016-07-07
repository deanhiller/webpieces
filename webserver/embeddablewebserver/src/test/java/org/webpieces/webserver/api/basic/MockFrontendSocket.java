package org.webpieces.webserver.api.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

public class MockFrontendSocket implements FrontendSocket {

	private List<HttpPayload> payloads = new ArrayList<>();
	
	@Override
	public CompletableFuture<FrontendSocket> close() {
		return null;
	}

	@Override
	public CompletableFuture<FrontendSocket> write(HttpPayload payload) {
		payloads.add(payload);
		return null;
	}

	@Override
	public Channel getUnderlyingChannel() {
		return null;
	}

	public List<HttpPayload> getResponses() {
		return payloads;
	}

}
