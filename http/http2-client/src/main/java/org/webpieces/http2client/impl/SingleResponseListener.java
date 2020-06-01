package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2client.api.exception.ServerRstStreamException;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class SingleResponseListener implements ResponseStreamHandle, StreamWriter {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<FullResponse> responseFuture = new CompletableFuture<FullResponse>();
	private Http2Response resp;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		this.resp = response;
		if(resp.isEndOfStream()) {
			responseFuture.complete(new FullResponse(resp, dataGen.emptyWrapper(), null));
			return CompletableFuture.completedFuture(null);
		}
		
		return CompletableFuture.completedFuture(this);
	}
	
	@Override
	public CompletableFuture<Void> processPiece(StreamMsg frame) {
		if(frame instanceof DataFrame) {
			incomingData((DataFrame) frame);
		} else if(frame instanceof RstStreamFrame) {
			serverCancelledRequest((RstStreamFrame) frame);
		} else if(frame instanceof Http2Trailers) {
			incomingEndHeaders((Http2Trailers) frame);
		} else
			throw new UnsupportedOperationException("missing use case. type="+frame.getClass()+" msg="+frame);
		
		//complete immediately because client is in control of single request/response
		//and can just send less requests if he wants to back off
		return CompletableFuture.completedFuture(null);
	}
	
	public void incomingData(DataFrame data) {
		fullData =  dataGen.chainDataWrappers(fullData, data.getData());
		if(data.isEndOfStream())
			responseFuture.complete(new FullResponse(resp, fullData, null));
	}

	public void incomingEndHeaders(Http2Trailers trailingHeaders) {
		if(!trailingHeaders.isEndOfStream()) {
			responseFuture.completeExceptionally(new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here"));
			throw new IllegalArgumentException("An assumption we made was wrong.  isComplete should be true here");
		}
		FullResponse response = new FullResponse(resp, fullData, trailingHeaders);
		responseFuture.complete(response);
	}

	public void serverCancelledRequest(RstStreamFrame response) {
		responseFuture.completeExceptionally(new ServerRstStreamException("Server cancelled this stream. code="+response.getErrorCode()));
	}

	public CompletableFuture<FullResponse> fetchResponseFuture() {
		return responseFuture;
	}

	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("you should either turn push promise setting off or not use single request/response since the server is sending a push_promise");
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason frame) {
		responseFuture.completeExceptionally(new ServerRstStreamException("The remote end reset this stream"));
		return CompletableFuture.completedFuture(null);
	}

}
