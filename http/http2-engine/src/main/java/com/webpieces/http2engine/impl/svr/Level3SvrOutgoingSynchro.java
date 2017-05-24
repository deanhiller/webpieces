package com.webpieces.http2engine.impl.svr;

import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.EngineStreamWriter;
import com.webpieces.http2engine.impl.client.Level3ClntOutgoingSyncro;
import com.webpieces.http2engine.impl.shared.Level3OutgoingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.PartialStream;
import com.webpieces.util.locking.FuturePermitQueue;
import com.webpieces.util.locking.PermitQueue;

public class Level3SvrOutgoingSynchro extends Level3OutgoingSynchro {

	private static final Logger log = LoggerFactory.getLogger(Level3ClntOutgoingSyncro.class);

	private Level4ServerStreams streams;

	public Level3SvrOutgoingSynchro(
			FuturePermitQueue serializer,
			PermitQueue maxConcurrent,
			Level4ServerStreams streams, 
			Level7MarshalAndPing marshalLayer, 
			HeaderSettings localSettings
	) {
		super(serializer, maxConcurrent, streams, marshalLayer, localSettings);
		this.streams = streams;
	}

	public CompletableFuture<StreamWriter> sendResponseHeaders(Stream stream, Http2Response data) {
		return singleThreadSerializer.runRequest(() -> {
			return streams.sendResponseHeaders(stream, data);
		}).thenApply(v -> new EngineStreamWriter(stream));
	}

	private class EngineStreamWriter implements StreamWriter {
		private Stream stream;

		public EngineStreamWriter(Stream stream) {
			this.stream = stream;
		}

		@Override
		public CompletableFuture<StreamWriter> processPiece(PartialStream data) {
			return singleThreadSerializer.runRequest(() -> {
				return streams.sendData(stream, data).thenApply(v->this);
			});
		}
	}
	
	public CompletableFuture<Void> sendCancel(Stream stream, RstStreamFrame frame) {
		return singleThreadSerializer.runRequest(() -> {
			return streams.fireRstToSocket(stream, frame);
		});
	}
	
	public CompletableFuture<ServerPushStream> sendPush(PushStreamHandleImpl handle, Http2Push push) {
		return singleThreadSerializer.runRequest(() -> {
			return streams.sendPush(handle, push);
		});
	}

	public CompletableFuture<Void> sendPushResponse(ServerPushStream stream, Http2Response response) {
		//This gets tricky, BUT must use the maxConcurrent permit queue first, THEN the serializer permit queue
		return maxConcurrentQueue.runRequest( () -> {
			int val = acquiredCnt.incrementAndGet();
			log.info("got push permit(cause="+response+").  size="+maxConcurrentQueue.availablePermits()+" acquired="+val);
			
			return singleThreadSerializer.runRequest(() -> {
				return streams.sendResponseHeaders(stream, response);
			});
		});
	}

	public CompletableFuture<Void> cancelPush(RstStreamFrame frame) {
		throw new UnsupportedOperationException("not yet");
	}

}
