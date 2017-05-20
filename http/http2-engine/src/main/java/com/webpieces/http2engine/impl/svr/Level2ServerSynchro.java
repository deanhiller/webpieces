package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.EngineStreamWriter;
import com.webpieces.http2engine.impl.shared.Level2Synchro;
import com.webpieces.http2engine.impl.shared.Level3ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Level2ServerSynchro extends Level2Synchro {

	private Level4ServerStreams streamInit;

	public Level2ServerSynchro(Level4ServerStreams level3, Level3ParsingAndRemoteSettings parsing, SessionExecutor executor) {
		super(level3, parsing, executor);
		streamInit = level3;
	}

	public CompletableFuture<StreamWriter> sendResponseHeaders(Stream stream, PartialStream data) {
		return executor.executeCall(this, () -> {
			int streamId = data.getStreamId();
			if(streamId <= 0)
				throw new IllegalArgumentException("frames for requests must have a streamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Server cannot send response frames with even stream ids to client per http/2 spec");
			
			return streamInit.sendToSocket(stream, data)
					.thenApply((s) -> new EngineStreamWriter(s, this));
		});
	}

	public CompletableFuture<StreamWriter> sendPush(Http2Push push) {
		return executor.executeCall(this, () -> {
			int streamId = push.getStreamId();
			int promisedId = push.getPromisedStreamId();
			if(streamId <= 0 || promisedId <= 0)
				throw new IllegalArgumentException("push frames for requests must have a streamId and promisedStreamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Server cannot send push frames with even stream ids to client per http/2 spec");
			else if(promisedId % 2 == 1)
				throw new IllegalArgumentException("Server cannot send push frames with odd promisedStreamId to client per http/2 spec");				

			return streamInit.sendPush(push)
					.thenApply((s) -> new EngineStreamWriter(s, this));
		});
	}

	public CompletableFuture<Void> sendCancel(Stream stream, RstStreamFrame frame) {
		return executor.executeCall(this, () -> {
			int streamId = frame.getStreamId();
			if(streamId <= 0)
				throw new IllegalArgumentException("frames for requests must have a streamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Server cannot send response frames with even stream ids to client per http/2 spec");
			
			return streamInit.fireRstToSocket(stream, frame)
					.thenApply((s) -> null);
		});
	}

}
