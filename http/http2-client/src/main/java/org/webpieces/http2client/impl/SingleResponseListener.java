package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.http2client.api.exceptions.ResetStreamException;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.Http2ResponseListener;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class SingleResponseListener implements Http2ResponseListener {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<Http2Response> responseFuture = new CompletableFuture<Http2Response>();
	private Http2Headers headers;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
		if(headers == null) {
			incomingResponse((Http2Headers) response);
		} else if(response instanceof DataFrame) {
			incomingData((DataFrame) response);
		} else
			incomingEndHeaders((Http2Headers) response);
		
		//complete immediately because client is in control of single request/response
		//and can just send less requests if he wants to back off
		return CompletableFuture.completedFuture(null);
	}
	
	public void incomingResponse(Http2Headers headers) {
		this.headers = headers;
		if(headers.isEndOfStream())
			responseFuture.complete(new Http2Response(headers, fullData, null));
	}

	public void incomingData(DataFrame data) {
		fullData =  dataGen.chainDataWrappers(fullData, data.getData());
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
