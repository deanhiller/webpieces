package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.time.Time;

import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.error.ConnectionException;
import com.webpieces.http2.api.dto.error.StreamException;
import com.webpieces.http2.api.dto.highlevel.Http2Headers;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.impl.shared.data.Stream;

/**
 * WAY TOOO MANY IF statements in here...need a ClientStreamState AND a ServerStreamState so we can get rid of the if statements
 * @author dhiller
 *
 */
public class StreamState {

	private final static Logger log = LoggerFactory.getLogger(StreamState.class);

	private ConcurrentMap<Integer, Stream> streamIdToStream = new ConcurrentHashMap<>();
	private long highestOddStream = 0;
	private long highestEvenStream = 0;
	
	//we need to time out closing streams  BUT just reuse the threads!!! do not use a timer thread
	private Time time;
	private String logId;

	public StreamState(Time time, String logId) {
		this.time = time;
		this.logId = logId;
	}

	//chanmgr thread only
	public ConcurrentMap<Integer, Stream> closeEngine() {
		return streamIdToStream;
	}
	
	public boolean isLargeEnough(Http2Headers frame) {
		int id = frame.getStreamId();
		if(id % 1 == 0) {
			if(id > highestOddStream)
				return true;
		} else {
			if(id > highestEvenStream)
				return true;
		}
		return false;
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
	
	public Stream getStream(Http2Msg frame, boolean isConnectionError) {
		Stream stream = streamIdToStream.get(frame.getStreamId());
		if (stream == null) {
			int id = frame.getStreamId();
			if(id % 2 == 0) {
				check(frame, id, highestEvenStream, isConnectionError);
			} else {
				check(frame, id, highestOddStream, isConnectionError);
			}			
		}
		return stream;
	}

	private void check(Http2Msg frame, int id, long highestOpen, boolean isConnectionError) {
		if(id > highestOpen)
			throw new ConnectionException(CancelReasonCode.BAD_FRAME_RECEIVED_FOR_THIS_STATE, logId, id, "Stream in idle state and received this frame which should not happen in idle state.  frame="+frame);
		else if(isConnectionError)
			throw new ConnectionException(CancelReasonCode.CLOSED_STREAM, logId, id, "Stream must have been closed as it no longer exists.  high mark="+highestOpen+"  your frame="+frame);
		
		throw new StreamException(CancelReasonCode.CLOSED_STREAM, logId, id, "Stream must have been closed as it no longer exists.  high mark="+highestOpen+"  your frame="+frame);
	}

	//this method and create happen on a virtual single thread from channelmgr
	//so we do not need to synchronize
	public void updateAllStreams(long initialWindow) {
		for(Stream stream : streamIdToStream.values()) {
			stream.updateInitialWindow(initialWindow);
		}
	}

	public Stream remove(Stream stream, Object cause) {
		stream.setIsClosed(true);
		return streamIdToStream.remove(stream.getStreamId());
	}

}
