package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level3AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level5RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level3ServerStreams extends Level3AbstractStreamMgr {

	private StreamState streamState;
	private Level4ServerStateMachine clientSm;
	private HeaderSettings localSettings;
	private volatile int streamsInProcess = 0;

	public Level3ServerStreams(StreamState streamState, Level4ServerStateMachine clientSm,
			Level5RemoteFlowControl remoteFlowCtrl, HeaderSettings localSettings, HeaderSettings remoteSettings) {
		super(remoteFlowCtrl, remoteSettings);
		this.streamState = streamState;
		this.clientSm = clientSm;
		this.localSettings = localSettings;
	}

	@Override
	public CompletableFuture<Void> sendPayloadToClient(PartialStream msg) {
		if(msg instanceof Http2Headers) {
			return processHeaders((Http2Headers) msg);
		} else
			throw new UnsupportedOperationException("not implemented yet="+msg);
	}

	private CompletableFuture<Void> processHeaders(Http2Headers msg) {
		Stream stream = createStream(msg.getStreamId());
		return clientSm.fireToClient(stream, msg, null).thenApply(s -> null);
	}
	
	private Stream createStream(int streamId) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		Stream stream = new Stream(streamId, initialState, null, null, localWindowSize, remoteWindowSize);
		return streamState.create(stream);
	}

	@Override
	protected void modifyMaxConcurrentStreams(long value) {
		//this is max promises to send at a time basically...we ignore for now
	}

}
