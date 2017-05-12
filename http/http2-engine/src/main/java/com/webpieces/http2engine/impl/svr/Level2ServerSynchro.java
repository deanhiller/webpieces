package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.threading.SessionExecutor;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.RequestWriterImpl;
import com.webpieces.http2engine.impl.shared.Level2Synchro;
import com.webpieces.http2engine.impl.shared.Level3ParsingAndRemoteSettings;
import com.webpieces.http2engine.impl.shared.Stream;

public class Level2ServerSynchro extends Level2Synchro {

	private Level4ServerStreams streamInit;

	public Level2ServerSynchro(Level4ServerStreams level3, Level3ParsingAndRemoteSettings parsing, SessionExecutor executor) {
		super(level3, parsing, executor);
		streamInit = level3;
	}

	public CompletableFuture<StreamWriter> sendResponseHeaders(Stream stream, Http2Headers data) {
		return executor.executeCall(this, () -> {
			int streamId = data.getStreamId();
			if(streamId <= 0)
				throw new IllegalArgumentException("frames for requests must have a streamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Server cannot send response frames with even stream ids to client per http/2 spec");
			
			return streamInit.sendResponseHeaderToSocket(stream, data)
					.thenApply((s) -> new RequestWriterImpl(s, this));
		});
	}
}
