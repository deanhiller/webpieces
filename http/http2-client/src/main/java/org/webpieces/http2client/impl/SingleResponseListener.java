package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.dto.Http2Headers;
import org.webpieces.http2client.api.dto.Http2Response;
import org.webpieces.http2client.api.exceptions.ResetStreamException;

import com.webpieces.http2parser.api.dto.Http2UnknownFrame;

public class SingleResponseListener implements Http2ResponseListener {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<Http2Response> responseFuture = new CompletableFuture<Http2Response>();
	private Http2Headers headers;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public void incomingResponse(Http2Headers headers, boolean isComplete) {
		this.headers = headers;
		if(isComplete)
			responseFuture.complete(new Http2Response(headers, fullData, null));
	}

	@Override
	public void incomingData(DataWrapper data, boolean isComplete) {
		fullData =  dataGen.chainDataWrappers(fullData, data);
		if(isComplete)
			responseFuture.complete(new Http2Response(headers, fullData, null));
	}

	@Override
	public void incomingEndHeaders(Http2Headers trailingHeaders, boolean isComplete) {
		if(!isComplete) {
			responseFuture.completeExceptionally(new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here"));
			throw new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here");
		}
		Http2Response response = new Http2Response(headers, fullData, trailingHeaders);
		responseFuture.complete(response);
	}

	@Override
	public void incomingUnknownFrame(Http2UnknownFrame frame, boolean isComplete) {
		//drop it for single request/response.  If they want this, don't use single request/response
	}
	
	@Override
	public void serverCancelledRequest() {
		responseFuture.completeExceptionally(new ResetStreamException("Server cancelled this request"));
	}

	public CompletableFuture<Http2Response> fetchResponseFuture() {
		return responseFuture;
	}

}
