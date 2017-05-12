package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.ConnectionClosedException;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2engine.impl.shared.Level4AbstractStreamMgr;
import com.webpieces.http2engine.impl.shared.Level6LocalFlowControl;
import com.webpieces.http2engine.impl.shared.Level6RemoteFlowControl;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2engine.impl.shared.StreamState;
import com.webpieces.http2parser.api.dto.PriorityFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level4ServerStreams extends Level4AbstractStreamMgr {

	private final static Logger log = LoggerFactory.getLogger(Level4ServerStreams.class);

	private Level5ServerStateMachine serverSm;
	private HeaderSettings localSettings;
	private volatile int streamsInProcess = 0;

	public Level4ServerStreams(StreamState streamState, Level5ServerStateMachine serverSm, Level6LocalFlowControl localFlowControl,
			Level6RemoteFlowControl remoteFlowCtrl, HeaderSettings localSettings, HeaderSettings remoteSettings) {
		super(serverSm, remoteFlowCtrl, localFlowControl, remoteSettings, streamState);
		this.serverSm = serverSm;
		this.localSettings = localSettings;
	}

	@Override
	public CompletableFuture<Void> sendPayloadToApp(PartialStream msg) {
		if(msg instanceof Http2Headers) {
			return processHeaders((Http2Headers) msg);
		} else
			throw new UnsupportedOperationException("not implemented yet="+msg);
	}

	private CompletableFuture<Void> processHeaders(Http2Headers msg) {
		Stream stream = createStream(msg.getStreamId());
		return serverSm.fireToClient(stream, msg, null).thenApply(s -> null);
	}
	
	private Stream createStream(int streamId) {
		Memento initialState = serverSm.createStateMachine("stream" + streamId);
		long localWindowSize = localSettings.getInitialWindowSize();
		long remoteWindowSize = remoteSettings.getInitialWindowSize();
		Stream stream = new Stream(streamId, initialState, null, null, localWindowSize, remoteWindowSize);
		return streamState.create(stream);
	}

	public CompletableFuture<Stream> sendResponseHeaderToSocket(Stream origStream, Http2Headers frame) {
		if(closedReason != null) {
			log.info("returning CompletableFuture.exception since this socket is closed(or closing)");
			CompletableFuture<Stream> future = new CompletableFuture<>();
			future.completeExceptionally(new ConnectionClosedException("Connection closed or closing", closedReason));
			return future;
		}
		Stream stream = streamState.getStream(frame);
		
		return serverSm.fireToSocket(stream, frame)
				.thenApply(s -> stream);
	}
	
	@Override
	protected void modifyMaxConcurrentStreams(long value) {
		//this is max promises to send at a time basically...we ignore for now
	}

	@Override
	public CompletableFuture<Void> sendPriorityFrame(PriorityFrame msg) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	protected CompletableFuture<Void> fireRstToSocket(Stream stream, RstStreamFrame frame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void release(PartialStream cause) {
	}

}
