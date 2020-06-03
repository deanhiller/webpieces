package com.webpieces.http2engine.impl;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.impl.client.Level3ClntOutgoingSyncro;
import com.webpieces.http2engine.impl.shared.data.Stream;

import java.util.concurrent.CompletableFuture;

/**
 * Request or Response StreamWriter
 */
public class EngineStreamWriter implements StreamWriter {

	private Stream stream;
	private Level3ClntOutgoingSyncro synchroLayer;
	private boolean streamEnded;

	public EngineStreamWriter(Stream stream, Level3ClntOutgoingSyncro level1) {
		this.stream = stream;
		this.synchroLayer = level1;
	}

	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {		
		if(data.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("PartialStream has incorrect stream id="+data
					+" it should be="+stream.getStreamId()+" since initial request piece had that id");
		else if(streamEnded)
			throw new IllegalArgumentException("Your client already sent in a PartialStream "
					+ "with endOfStream=true.  you can't send more data. offending data="+data);
		
		if(data.isEndOfStream())
			streamEnded = true;
		
		return synchroLayer.sendDataToSocket(stream, data);
	}

}
