package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.PushPromiseListener;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.http2client.api.exceptions.ResetStreamException;

import com.webpieces.http2engine.api.Http2Data;
import com.webpieces.http2engine.api.Http2Headers;
import com.webpieces.http2engine.api.PartialStream;

public class SingleResponseListener implements Http2ResponseListener {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<Http2Response> responseFuture = new CompletableFuture<Http2Response>();
	private Http2Headers headers;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public void incomingPartialResponse(PartialStream response) {
		if(headers == null) {
			incomingResponse((Http2Headers) response);
		} else if(response instanceof Http2Data) {
			incomingData((Http2Data) response);
		} else
			incomingEndHeaders((Http2Headers) response);
	}
	
	public void incomingResponse(Http2Headers headers) {
		this.headers = headers;
		if(headers.isEndOfStream())
			responseFuture.complete(new Http2Response(headers, fullData, null));
	}

	public void incomingData(Http2Data data) {
		fullData =  dataGen.chainDataWrappers(fullData, data.getPayload());
		if(data.isEndOfStream())
			responseFuture.complete(new Http2Response(headers, fullData, null));
	}

	public void incomingEndHeaders(Http2Headers trailingHeaders) {
		if(!trailingHeaders.isEndOfStream()) {
			responseFuture.completeExceptionally(new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here"));
			throw new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here");
		}
		Http2Response response = new Http2Response(headers, fullData, trailingHeaders);
		responseFuture.complete(response);
	}

	@Override
	public void serverCancelledRequest() {
		responseFuture.completeExceptionally(new ResetStreamException("Server cancelled this request"));
	}

	public CompletableFuture<Http2Response> fetchResponseFuture() {
		return responseFuture;
	}


	@Override
	public PushPromiseListener newIncomingPush(int streamId) {
		throw new UnsupportedOperationException("you should either turn push promise setting off or not use single request/response since the server is sending a push_promise");
	}

}
