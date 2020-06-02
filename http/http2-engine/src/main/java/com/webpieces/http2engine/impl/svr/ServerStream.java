package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamRef;
import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.RequestStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ServerStream extends Stream {

	private RequestStreamHandle streamHandle;
	private StreamRef streamRef;

	public ServerStream(String logId, int streamId, Memento currentState,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, false);
	}

	public CompletableFuture<StreamWriter> getStreamWriter() {
		return streamRef.getWriter();
	}

	public void setStreamHandle(RequestStreamHandle streamHandle, StreamRef streamRef) {
		this.streamHandle = streamHandle;
		this.streamRef = streamRef;
	}

	public RequestStreamHandle getStreamHandle() {
		return streamHandle;
	}

	public StreamRef getStreamRef() {
		return streamRef;
	}
}
