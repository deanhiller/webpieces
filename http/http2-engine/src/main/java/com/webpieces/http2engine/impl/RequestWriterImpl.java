package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.impl.shared.Level2Synchro;
import com.webpieces.http2engine.impl.shared.Stream;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class RequestWriterImpl implements StreamWriter {

	private Stream stream;
	private Level2Synchro level1;
	private boolean streamEnded;

	public RequestWriterImpl(Stream stream, Level2Synchro level1) {
		this.stream = stream;
		this.level1 = level1;
	}

	@Override
	public CompletableFuture<StreamWriter> send(PartialStream data) {
		if(data.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("PartialStream has incorrect stream id="+data
					+" it should be="+stream.getStreamId()+" since initial request piece had that id");
		else if(streamEnded)
			throw new IllegalArgumentException("Your client already sent in a PartialStream "
					+ "with endOfStream=true.  you can't send more data. offending data="+data);
		
		if(data.isEndOfStream())
			streamEnded = true;
		
		return level1.sendMoreStreamData(stream, data).thenApply(c -> this);
	}

}
