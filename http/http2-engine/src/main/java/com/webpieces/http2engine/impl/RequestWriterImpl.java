package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.RequestWriter;
import com.webpieces.http2engine.api.dto.PartialStream;

public class RequestWriterImpl implements RequestWriter {

	private Stream stream;
	private Level4ClientStateMachine clientSm;
	private boolean streamEnded;

	public RequestWriterImpl(Stream stream, Level4ClientStateMachine clientSm) {
		this.stream = stream;
		this.clientSm = clientSm;
	}

	@Override
	public CompletableFuture<RequestWriter> sendMore(PartialStream data) {
		if(data.getStreamId() != stream.getStreamId())
			throw new IllegalArgumentException("PartialStream has incorrect stream id="+data
					+" it should be="+stream.getStreamId()+" since initial request piece had that id");
		else if(streamEnded)
			throw new IllegalArgumentException("Your client already sent in a PartialStream "
					+ "with endOfStream=true.  you can't send more data. offending data="+data);
		
		if(data.isEndOfStream())
			streamEnded = true;
		
		return clientSm.fireToSocket(stream.getCurrentState(), data).thenApply(c -> this);
	}

}
