package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class RequestWriterImpl implements StreamWriter {

	private Stream stream;
	private Level3StreamInitialization clientSm;
	private boolean streamEnded;

	public RequestWriterImpl(Stream stream, Level3StreamInitialization clientSm) {
		this.stream = stream;
		this.clientSm = clientSm;
	}

	@Override
	public CompletableFuture<StreamWriter> sendMore(PartialStream data) {
		if(data.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("PartialStream has incorrect stream id="+data
					+" it should be="+stream.getStreamId()+" since initial request piece had that id");
		else if(streamEnded)
			throw new IllegalArgumentException("Your client already sent in a PartialStream "
					+ "with endOfStream=true.  you can't send more data. offending data="+data);
		
		if(data.isEndOfStream())
			streamEnded = true;
		
		return clientSm.sendMoreStreamData(stream, data).thenApply(c -> this);
	}

}
