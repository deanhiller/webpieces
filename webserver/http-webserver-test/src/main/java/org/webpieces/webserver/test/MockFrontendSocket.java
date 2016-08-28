package org.webpieces.webserver.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;

public class MockFrontendSocket implements FrontendSocket {

	private static final Logger log = LoggerFactory.getLogger(MockFrontendSocket.class);
	private List<FullResponse> payloads = new ArrayList<>();
	private FullResponse chunkedResponse;
	
	@Override
	public CompletableFuture<FrontendSocket> close() {
		return null;
	}

	@Override
	public synchronized CompletableFuture<FrontendSocket> write(HttpPayload payload) {
		if(chunkedResponse == null) {
			HttpResponse response = payload.getHttpResponse();
			if(response == null) {
				log.warn("should be receiving http response but received="+payload);
				return null;
			}
			FullResponse nextResp = new FullResponse(response);
			if(response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH) != null)
				payloads.add(nextResp);
			else
				chunkedResponse = nextResp;
			
			return null;
		} else if(payloads.size() == 0) {
			log.error("Should get HttpResponse first but instead received something else="+payload);
			return null;
		}
		
		switch (payload.getMessageType()) {
		case CHUNK:
			chunkedResponse.addChunk(payload.getHttpChunk());
			break;
		case LAST_CHUNK:
			chunkedResponse.setLastChunk(payload.getLastHttpChunk());
			payloads.add(chunkedResponse);
			chunkedResponse = null;
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

	public synchronized List<FullResponse> getResponses(long waitTimeMs, int count) {
		try {
			return getResponsesImpl(waitTimeMs, count);
		} catch (InterruptedException e) {
			throw new RuntimeException("failed waiting", e);
		}
	}
	
	public synchronized List<FullResponse> getResponsesImpl(long waitTimeMs, int count) throws InterruptedException {
		long start = System.currentTimeMillis();
		while(payloads.size() < count) {
			this.wait(waitTimeMs+500);
			long time = System.currentTimeMillis() - start;
			if(time > waitTimeMs)
				throw new IllegalStateException("While waiting for "+count+" responses, some or all never came.  count that came="+payloads.size());
		}
		
		return payloads;
	}
	
	public void clear() {
		payloads.clear();
		chunkedResponse = null;
	}

}
