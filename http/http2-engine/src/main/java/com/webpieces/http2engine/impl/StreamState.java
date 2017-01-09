package com.webpieces.http2engine.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class StreamState {

	private ConcurrentHashMap<Integer, Stream> streamIdToStream = new ConcurrentHashMap<>();

	public Stream create(Stream stream) {
		Stream oldStream = streamIdToStream.putIfAbsent(stream.getStreamId(), stream);
		if(oldStream == stream)
			throw new IllegalStateException("stream id="+stream.getStreamId()+" already exists");
		return stream;
	}
	
	public Stream get(Http2Msg frame) {
		Stream stream = streamIdToStream.get(frame.getStreamId());
		if (stream == null)
			throw new IllegalArgumentException("bug, Stream not found for frame="+frame);
		return stream;
	}

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
