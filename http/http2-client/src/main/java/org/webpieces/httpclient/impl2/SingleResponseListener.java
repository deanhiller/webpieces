package org.webpieces.httpclient.impl2;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.dto.Http2Headers;
import org.webpieces.httpclient.api.dto.Http2Response;
import org.webpieces.httpclient.api.exceptions.ResetStreamException;

import com.webpieces.http2parser.api.dto.Http2UnknownFrame;

public class SingleResponseListener implements Http2ResponseListener {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<Http2Response> responseFuture = new CompletableFuture<Http2Response>();
	private Http2Headers headers;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public void incomingResponse(Http2Headers headers) {
		this.headers = headers;
	}

	@Override
	public void incomingData(DataWrapper data) {
		fullData =  dataGen.chainDataWrappers(fullData, data);
	}

	@Override
	public void incomingEndHeaders(Http2Headers trailingHeaders) {
		Http2Response response = new Http2Response(headers, fullData, trailingHeaders);
		responseFuture.complete(response);
	}

	@Override
	public void incomingUnknownFrame(Http2UnknownFrame frame) {
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
