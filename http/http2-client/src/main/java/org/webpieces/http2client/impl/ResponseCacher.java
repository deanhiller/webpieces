package org.webpieces.http2client.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Trailers;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ResponseCacher {

	private Supplier<RequestStreamHandle> openStreamFunc;

	public ResponseCacher(Supplier<RequestStreamHandle> openStreamFunc) {
		this.openStreamFunc = openStreamFunc;
	}

	public CompletableFuture<FullResponse> run(FullRequest request) {
		SingleResponseListener responseListener = new SingleResponseListener();
		
		RequestStreamHandle streamHandle = openStreamFunc.get();
		
		Http2Request req = request.getHeaders();
		
		if(request.getPayload() == null) {
			request.getHeaders().setEndOfStream(true);
			streamHandle.process(req, responseListener);
			return responseListener.fetchResponseFuture();
		} else if(request.getTrailingHeaders() == null) {
			request.getHeaders().setEndOfStream(false);
			DataFrame data = createData(request, true);

			StreamRef streamRef = streamHandle.process(request.getHeaders(), responseListener);

			return streamRef.getWriter()
						.thenCompose(writer -> {
							data.setStreamId(req.getStreamId());
							return writer.processPiece(data);
						})
						.thenCompose(writer -> responseListener.fetchResponseFuture());
		}
		
		request.getHeaders().setEndOfStream(false);
		DataFrame data = createData(request, false);
		Http2Trailers trailers = request.getTrailingHeaders();
		trailers.setEndOfStream(true);


		StreamRef streamRef = streamHandle.process(request.getHeaders(), responseListener);

		return streamRef.getWriter()
				.thenCompose(writer -> writeStuff(writer, req, data, trailers, responseListener));
	}

	private DataFrame createData(FullRequest request, boolean isEndOfStream) {
		DataWrapper payload = request.getPayload();
		DataFrame data = new DataFrame();
		data.setEndOfStream(isEndOfStream);
		data.setData(payload);
		return data;
	}
	
	private CompletableFuture<FullResponse> writeStuff(
			StreamWriter writer, Http2Request req, DataFrame data, Http2Trailers trailers, SingleResponseListener responseListener) {
		
		data.setStreamId(req.getStreamId());
		return writer.processPiece(data)
						.thenCompose(v -> {
							trailers.setStreamId(req.getStreamId());
							return writer.processPiece(trailers);
						})
						.thenCompose(v -> responseListener.fetchResponseFuture());
	}
}
