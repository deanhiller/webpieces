package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class StreamState {

	private ConcurrentMap<Integer, Stream> streamIdToStream = new ConcurrentHashMap<>();
	//private AtomicReference<Function<Stream, Stream>> createFunction = new AtomicReference<Function<Stream,Stream>>((s) -> create(s));

	//chanmgr thread only
	public ConcurrentMap<Integer, Stream> closeEngine(ConnectionException e) {
		return streamIdToStream;
	}
	
	//client threads
	public Stream create(Stream stream) {
		Stream oldStream = streamIdToStream.putIfAbsent(stream.getStreamId(), stream);
		if(oldStream == stream)
			throw new IllegalStateException("stream id="+stream.getStreamId()+" already exists");
		return stream;
	}
	
	public Stream getStream(Http2Msg frame) {
		Stream stream = streamIdToStream.get(frame.getStreamId());
		if (stream == null)
			throw new IllegalArgumentException("bug, Stream not found for frame="+frame);
		return stream;
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
