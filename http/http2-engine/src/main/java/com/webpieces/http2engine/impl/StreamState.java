package com.webpieces.http2engine.impl;

import java.util.HashMap;
import java.util.Map;

public class StreamState {

	private Map<Integer, Stream> streamIdToStream = new HashMap<>();

	public Stream get(int streamId) {
		return streamIdToStream.get(streamId);
	}

	public void put(int streamId, Stream stream) {
		streamIdToStream.put(streamId, stream);
	}

	
}
