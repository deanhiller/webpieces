package com.webpieces.http2engine.impl.svr;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class ServerStream extends Stream {

	private StreamWriter streamWriter;
	private StreamHandle streamHandle;

	public ServerStream(String logId, int streamId, Memento currentState,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(logId, streamId, currentState, localInitialWindowSize, remoteInitialWindowSize, false);
	}

	public void setStreamWriter(StreamWriter w) {
		this.streamWriter = w;
	}

	public StreamWriter getStreamWriter() {
		return streamWriter;
	}

	public void setStreamHandle(StreamHandle streamHandle) {
		this.streamHandle = streamHandle;
	}

	public StreamHandle getStreamHandle() {
		return streamHandle;
	}

}
