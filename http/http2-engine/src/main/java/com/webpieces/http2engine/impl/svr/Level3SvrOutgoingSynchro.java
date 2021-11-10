package com.webpieces.http2engine.impl.svr;

import org.webpieces.util.futures.XFuture;

import org.webpieces.util.locking.PermitQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.RstStreamFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.impl.client.Level3ClntOutgoingSyncro;
import com.webpieces.http2engine.impl.shared.Level3OutgoingSynchro;
import com.webpieces.http2engine.impl.shared.Level7MarshalAndPing;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;
import com.webpieces.http2engine.impl.shared.data.Stream;

public class Level3SvrOutgoingSynchro extends Level3OutgoingSynchro {

	private static final Logger log = LoggerFactory.getLogger(Level3ClntOutgoingSyncro.class);

	private Level4ServerPreconditions streams;

	public Level3SvrOutgoingSynchro(
			PermitQueue maxConcurrent,
			Level4ServerPreconditions streams, 
			Level7MarshalAndPing marshalLayer, 
			HeaderSettings localSettings
	) {
		super(maxConcurrent, streams, marshalLayer, localSettings);
		this.streams = streams;
	}

	public XFuture<StreamWriter> sendResponseToSocket(Stream stream, Http2Response data) {
		return streams.sendResponseToSocket(stream, data)
					.thenApply(v -> new EngineStreamWriter(stream));
	}

	private class EngineStreamWriter implements StreamWriter {
		private Stream stream;

		public EngineStreamWriter(Stream stream) {
			this.stream = stream;
		}

		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			data.setStreamId(stream.getStreamId());
			return streams.sendDataToSocket(stream, data);
		}
		
		public XFuture<Void> cancel(CancelReason frame) {
			if(!(frame instanceof RstStreamFrame))
				throw new IllegalArgumentException("App can only pass in RstStreamFrame object here to be sent to clients.  The api is for consistency and shared with client");
			
			int streamId = frame.getStreamId();
			if(streamId <= 0)
				throw new IllegalArgumentException("frames for requests must have a streamId > 0");
			else if(streamId % 2 == 0)
				throw new IllegalArgumentException("Server cannot send response frames with even stream ids to client per http/2 spec");
			
			return sendRstToSocket(stream, (RstStreamFrame)frame);
		}
	}
	
	public XFuture<Void> sendRstToSocket(Stream stream, RstStreamFrame frame) {
		return streams.sendRstToSocket(stream, frame);
	}
	
	public XFuture<ServerPushStream> sendPushToSocket(PushStreamHandleImpl handle, Http2Push push) {
		return streams.sendPush(handle, push);
	}

	public XFuture<Void> sendPushResponseToSocket(ServerPushStream stream, Http2Response response) {
		//This gets tricky, BUT must use the maxConcurrent permit queue first, THEN the serializer permit queue
		return maxConcurrentQueue.runRequest( () -> {
			int val = acquiredCnt.incrementAndGet();
			log.info("got push permit(cause="+response+").  size="+maxConcurrentQueue.availablePermits()+" acquired="+val);
			
			stream.setHasPermit(true);
			
			return streams.sendResponseToSocket(stream, response);
		});
	}

	public XFuture<Void> sendPushRstToSocket(CancelReason reset) {
		throw new UnsupportedOperationException("not yet");
	}

}
