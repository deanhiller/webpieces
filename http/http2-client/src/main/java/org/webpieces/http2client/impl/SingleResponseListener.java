package org.webpieces.http2client.impl;

import org.webpieces.util.futures.XFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.http2client.api.exception.ServerRstStreamException;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class SingleResponseListener implements ResponseStreamHandle, StreamWriter {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private XFuture<FullResponse> responseFuture = new XFuture<FullResponse>();
	private Http2Response resp;
	private DataWrapper fullData = dataGen.emptyWrapper();
	
	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		this.resp = response;
		if(resp.isEndOfStream()) {
			responseFuture.complete(new FullResponse(resp, dataGen.emptyWrapper(), null));
			return XFuture.completedFuture(null);
		}
				
		return XFuture.completedFuture(this);
	}
	
	@Override
	public XFuture<Void> processPiece(StreamMsg frame) {
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
		return XFuture.completedFuture(null);
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

	public XFuture<FullResponse> fetchResponseFuture() {
		return responseFuture;
	}

	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException("you should either turn push promise setting off or not use single request/response since the server is sending a push_promise");
	}

	@Override
	public XFuture<Void> cancel(CancelReason frame) {
		responseFuture.completeExceptionally(new ServerRstStreamException("The remote end reset this stream. reason="+frame));
		return XFuture.completedFuture(null);
	}

}
