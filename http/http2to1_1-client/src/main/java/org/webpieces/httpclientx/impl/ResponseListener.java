package org.webpieces.httpclientx.impl;

import org.webpieces.util.futures.XFuture;

import org.webpieces.http2translations.api.Http11ToHttp2;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.api.error.ConnectionCancelled;
import com.webpieces.http2engine.api.error.ConnectionFailure;
import com.webpieces.http2engine.api.error.ReceivedGoAway;
import com.webpieces.http2engine.api.error.ShutdownStream;

public class ResponseListener implements HttpResponseListener {

	private ResponseStreamHandle responseListener;
	private String logId;

	public ResponseListener(String logId, ResponseStreamHandle responseListener) {
		this.logId = logId;
		this.responseListener = responseListener;
	}

	@Override
	public XFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		Http2Response r = Http11ToHttp2.responseToHeaders(resp);
		return responseListener.process(r).thenApply(w -> new DataWriterImpl(w));
	}
	
	private class DataWriterImpl implements HttpDataWriter {
		private StreamWriter writer;
		public DataWriterImpl(StreamWriter writer) {
			this.writer = writer;
		}

		@Override
		public XFuture<Void> send(HttpData chunk) {
			DataFrame data = Http11ToHttp2.translateData(chunk);
			return writer.processPiece(data);
		}
	}
	@Override
	public void failure(Throwable e) {
		ConnectionCancelled connCancelled = new ConnectionFailure(new ConnectionException(CancelReasonCode.BUG, logId, 0, "Failure from connection", e));
		responseListener.cancel(new ShutdownStream(0, connCancelled));
	}

}
