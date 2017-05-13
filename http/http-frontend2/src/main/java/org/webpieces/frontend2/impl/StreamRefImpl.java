package org.webpieces.frontend2.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.HttpRequestListener;

import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.server.StreamReference;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class StreamRefImpl implements StreamReference {

	private Http2StreamImpl stream;
	private HttpRequestListener httpListener;
	private StreamWriter writer;

	public StreamRefImpl(Http2StreamImpl stream, HttpRequestListener httpListener, StreamWriter writer) {
		this.stream = stream;
		this.httpListener = httpListener;
		this.writer = writer;
	}

	@Override
	public CompletableFuture<StreamWriter> sendMore(PartialStream data) {
		return writer.send(data);
	}

	@Override
	public CompletableFuture<Void> cancel(ConnectionReset c) {
		httpListener.cancelRequest(stream, c);
		return CompletableFuture.completedFuture(null);
	}

}
