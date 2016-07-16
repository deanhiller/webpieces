package org.webpieces.webserver.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

public class MockFrontendSocket implements FrontendSocket {

	private static final Logger log = LoggerFactory.getLogger(MockFrontendSocket.class);
	private List<FullResponse> payloads = new ArrayList<>();
	private boolean waitingResponseStart = true;
	
	@Override
	public CompletableFuture<FrontendSocket> close() {
		return null;
	}

	@Override
	public CompletableFuture<FrontendSocket> write(HttpPayload payload) {
		if(waitingResponseStart) {
			HttpResponse response = payload.getHttpResponse();
			if(response == null) {
				log.warn("should be receiving http response but received="+payload);
				return null;
			}
			payloads.add(new FullResponse(response));
			waitingResponseStart = false;
			return null;
		} else if(payloads.size() == 0) {
			log.error("Should get HttpResponse first but instead received something else="+payload);
			return null;
		}
		
		FullResponse fullResponse = payloads.get(payloads.size()-1);
		
		switch (payload.getMessageType()) {
		case CHUNK:
			fullResponse.addChunk(payload.getHttpChunk());
			break;
		case LAST_CHUNK:
			fullResponse.setLastChunk(payload.getLastHttpChunk());
			waitingResponseStart = true;
			break;
		default:
			log.error("expecting chunk but received payload="+payload);
			return null;
		}
		
		return null;
	}

	@Override
	public Channel getUnderlyingChannel() {
		return null;
	}

	public List<FullResponse> getResponses() {
		return payloads;
	}

}
