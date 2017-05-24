package com.webpieces.http2engine.impl.svr;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.impl.shared.data.Stream;

public class ServerPushStream extends Stream {


	private PushStreamHandleImpl handle;

	public ServerPushStream(PushStreamHandleImpl handle, int streamId, Memento currentState,
			long localInitialWindowSize, long remoteInitialWindowSize) {
		super(streamId, currentState, localInitialWindowSize, remoteInitialWindowSize);
		this.handle = handle;
	}

	public PushStreamHandleImpl getHandle() {
		return handle;
	}
	

}
