package org.webpieces.frontend2.impl.proxy;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.exceptions.NioClosedChannelException;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class ProxyStreamWriter implements StreamWriter {

	private StreamWriter writer;
	private Queue<Object> lastFewFrames;

	public ProxyStreamWriter(StreamWriter writer, Queue<Object> lastFewFrames) {
		this.writer = writer;
		this.lastFewFrames = lastFewFrames;
	}

	@Override
	public CompletableFuture<Void> processPiece(StreamMsg data) {
		lastFewFrames.add(data);
		if(lastFewFrames.size() >= 4)
			lastFewFrames.poll(); //remove one, keep the queue small
		
		try {
			return writer.processPiece(data);
		} catch(NioClosedChannelException e) {
			throw new NioClosedChannelException("last few frames="+lastFewFrames, e);
		}
	}

}
