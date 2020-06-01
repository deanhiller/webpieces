package org.webpieces.httpclientx.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.http2translations.api.Http11ToHttp2;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ConnectionFailure;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.ConnectionException;

public class ResponseListener implements HttpResponseListener {

	private ResponseStreamHandle responseListener;
	private String logId;
	private StreamRef streamRef;

	public ResponseListener(String logId, ResponseStreamHandle responseListener) {
		this.logId = logId;
		this.responseListener = responseListener;
	}

	@Override
	public HttpStreamRef incomingResponse(HttpResponse resp, boolean isComplete) {
		Http2Response r = Http11ToHttp2.responseToHeaders(resp);
		
		streamRef = responseListener.process(r);
		CompletableFuture<HttpDataWriter> newWriter = streamRef.getWriter().thenApply(w -> new DataWriterImpl(w));
		return new MyStreamRef(streamRef, newWriter);
	}

	private class MyStreamRef implements HttpStreamRef {

		private StreamRef streamRef;
		private CompletableFuture<HttpDataWriter> newWriter;

		public MyStreamRef(StreamRef streamRef, CompletableFuture<HttpDataWriter> newWriter) {
			this.streamRef = streamRef;
			this.newWriter = newWriter;
		}

		@Override
		public CompletableFuture<HttpDataWriter> getWriter() {
			return newWriter;
		}

		@Override
		public CompletableFuture<Void> cancel(Object reason) {
			return streamRef.cancel((CancelReason) reason);
		}
	}
	
	private class DataWriterImpl implements HttpDataWriter {
		private StreamWriter writer;
		public DataWriterImpl(StreamWriter writer) {
			this.writer = writer;
		}

		@Override
		public CompletableFuture<Void> send(HttpData chunk) {
			DataFrame data = Http11ToHttp2.translateData(chunk);
			return writer.processPiece(data);
		}
	}
	@Override
	public void failure(Throwable e) { 		
		if(streamRef != null) {
			ConnectionCancelled connCancelled = new ConnectionFailure(new ConnectionException(CancelReasonCode.BUG, logId, 0, "Failure from connection", e));
			streamRef.cancel(new ShutdownStream(0, connCancelled));
		}
	}

}
