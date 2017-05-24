package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.Synchro;
import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

/**
 * Request or Response StreamWriter
 */
public class EngineStreamWriter implements StreamWriter {

	private Stream stream;
	private Synchro synchroLayer;
	private boolean streamEnded;

	public EngineStreamWriter(Stream stream, Synchro level1) {
		this.stream = stream;
		this.synchroLayer = level1;
	}

	@Override
	public CompletableFuture<StreamWriter> processPiece(PartialStream data) {		
		if(data.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("PartialStream has incorrect stream id="+data
					+" it should be="+stream.getStreamId()+" since initial request piece had that id");
		else if(streamEnded)
			throw new IllegalArgumentException("Your client already sent in a PartialStream "
					+ "with endOfStream=true.  you can't send more data. offending data="+data);
		
		if(data.isEndOfStream())
			streamEnded = true;
		
		return synchroLayer.sendData(stream, data).thenApply(c -> this);
	}

}
