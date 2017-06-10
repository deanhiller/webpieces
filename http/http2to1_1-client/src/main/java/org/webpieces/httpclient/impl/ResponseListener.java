package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpclient.api.DataWriter;
import org.webpieces.httpclient.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ConnectionFailure;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.ConnectionException;

public class ResponseListener implements HttpResponseListener {

	private ResponseHandler responseListener;
	private String logId;

	public ResponseListener(String logId, ResponseHandler responseListener) {
		this.logId = logId;
		this.responseListener = responseListener;
	}

	@Override
	public CompletableFuture<DataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		Http2Response r = Translations.translate(resp);
		return responseListener.process(r).thenApply(w -> new DataWriterImpl(w));
	}

	private class DataWriterImpl implements DataWriter {
		private StreamWriter writer;
		public DataWriterImpl(StreamWriter writer) {
			this.writer = writer;
		}

		@Override
		public CompletableFuture<DataWriter> incomingData(HttpData chunk) {
			DataFrame data = Translations.translate(chunk);
			return writer.processPiece(data).thenApply(c -> this);
		}
	}
	@Override
	public void failure(Throwable e) {
		ConnectionCancelled connCancelled = new ConnectionFailure(new ConnectionException(CancelReasonCode.BUG, logId, 0, "Failure from connection", e));
		responseListener.cancel(new ShutdownStream(0, connCancelled));
	}

}
