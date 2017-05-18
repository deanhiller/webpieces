package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.util.time.Time;

public class StreamState {

	private ConcurrentMap<Integer, Stream> streamIdToStream = new ConcurrentHashMap<>();
	private long highestOddStream = 0;
	private long highestEvenStream = 0;
	
	//we need to time out closing streams  BUT just reuse the threads!!! do not use a timer thread
	private Time time;

	public StreamState(Time time) {
		this.time = time;
	}

	//chanmgr thread only
	public ConcurrentMap<Integer, Stream> closeEngine() {
		return streamIdToStream;
	}
	
	//client threads
	public Stream create(Stream stream) {
		int id = stream.getStreamId();
		if(id % 2 == 0) {
			if(id < highestEvenStream)
				throw new IllegalStateException("stream id="+id+" is too low and must be higher than="+highestEvenStream);
			highestEvenStream = id;
		} else {
			if(id < highestOddStream)
				throw new IllegalStateException("stream id="+id+" is too low and must be higher than="+highestOddStream);
			highestOddStream = id;			
		}
			
		Stream oldStream = streamIdToStream.putIfAbsent(id, stream);
		if(oldStream == stream)
			throw new IllegalStateException("stream id="+id+" already exists");
		return stream;
	}
	
	public boolean isStreamExist(Http2Msg frame) {
		Stream stream = streamIdToStream.get(frame.getStreamId());
		return stream != null;
	}
	
	public Stream getStream(Http2Msg frame) {
		Stream stream = streamIdToStream.get(frame.getStreamId());
		if (stream == null) {
			int id = frame.getStreamId();
			if(id % 2 == 0) {
				check(frame, id, highestEvenStream);
			} else {
				check(frame, id, highestOddStream);
			}			
		}
		return stream;
	}

	private void check(Http2Msg frame, int id, long highestOpen) {
		if(id > highestOpen)
			throw new ConnectionException(ParseFailReason.BAD_FRAME_RECEIVED_FOR_THIS_STATE, id, "Stream in idle state and received this frame which should not happen in idle state.  frame="+frame); 
		throw new ConnectionException(ParseFailReason.CLOSED_STREAM, id, "Stream must have been closed as it no longer exists.  high mark="+highestOpen+"  your frame="+frame);
	}

	//this method and create happen on a virtual single thread from channelmgr
	//so we do not need to synchronize
	public void updateAllStreams(long initialWindow) {
		for(Stream stream : streamIdToStream.values()) {
			stream.updateInitialWindow(initialWindow);
		}
	}

	public Stream remove(Stream stream) {
		stream.setIsClosed(true);
		return streamIdToStream.remove(stream.getStreamId());
	}

	
}
