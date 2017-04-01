package org.webpieces.webserver.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.Protocol;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class MockResponseSender implements ResponseSender {

	private static final Logger log = LoggerFactory.getLogger(MockResponseSender.class);
	private List<FullResponse> payloads = new ArrayList<>();
	private FullResponse chunkedResponse;

	@Override
	public Protocol getProtocol() {
		return null;
	}

	@Override
	public CompletableFuture<Void> close() {
		return null;
	}

	@Override
	public CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isLastData) {
		if(isLastData) {
			log.info("last chunk");
			HttpLastChunk chunk = new HttpLastChunk();
			chunk.setBody(data);
			chunkedResponse.setLastChunk(chunk);
			payloads.add(chunkedResponse);
			chunkedResponse = null;
			synchronized (this) { this.notifyAll(); }
		} else {
			HttpChunk chunk = new HttpChunk();
			chunk.setBody(data);
			chunkedResponse.addChunk(chunk);
		}

		return null;
	}

	@Override
	public void sendTrailer(List<Http2Header> headerList, ResponseId id, boolean isComplete) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompletableFuture<Void> sendException(HttpException e) {
		return null;
	}

	@Override
	public synchronized CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
		if(chunkedResponse == null) {
			FullResponse nextResp = new FullResponse(response);
			if (response.getHeaderLookupStruct().getHeader(KnownHeaderName.CONTENT_LENGTH) != null) {
				payloads.add(nextResp);
				synchronized (this) { this.notifyAll(); }
			} else
				chunkedResponse = nextResp;
		}
		else {
			log.error("expecting sendData but got Response instead=" + response);
		}

		return CompletableFuture.completedFuture(new ResponseId(0));
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
			if(payloads.size() >= count)
				return payloads;
			
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
