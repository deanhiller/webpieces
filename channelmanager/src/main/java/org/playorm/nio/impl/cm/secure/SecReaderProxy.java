package org.playorm.nio.impl.cm.secure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.PacketAction;
import org.playorm.nio.impl.util.DataChunkWithBuffer;


class SecReaderProxy implements DataListener {
	
	private static final Logger log = Logger.getLogger(SecReaderProxy.class.getName());
	private AsyncSSLEngine handler;
	private SecSSLListener sslListener;
	private ByteBuffer data = ByteBuffer.allocate(2000);
	private boolean isClosed;
	
	public SecReaderProxy(SecSSLListener sslListener) {
		this.sslListener = sslListener;
	}
	
	public ByteBuffer getBuffer(Channel c) {
		return data;
	}

	public void incomingData(Channel c, DataChunk chunk) throws IOException {
		ByteBuffer b = chunk.getData();
		if(!isClosed) {
			PacketAction action = handler.feedEncryptedPacket(b, chunk);
			if(action == PacketAction.NOT_ENOUGH_BYTES_YET) {
				chunk.setProcessed("SecReaderProxy"); //trigger another read from socket
			}
			
		} else {
			b.position(b.limit()); //if closed, read the data so we don't get warnings
			chunk.setProcessed("SecReaderProxy");
		}

		DataChunkWithBuffer cb = (DataChunkWithBuffer) chunk;
		cb.releaseBuffer(" hander that did not consume all data="+handler);
	}
	
	public void farEndClosed(Channel c) {
		handler.close();
		sslListener.farEndClosed();
	}

	public void setHandler(AsyncSSLEngine handler) {
		this.handler = handler;
	}

	public AsyncSSLEngine getHandler() {
		return handler;
	}

	public void close() {
		isClosed = true;
		if(handler != null)
			handler.close();
	}

	public void failure(Channel c, ByteBuffer data, Exception e) {
		try {
			sslListener.feedProblemThrough(c, data, e);
		} catch (IOException e1) {
			RuntimeException exc = new RuntimeException(e1.getMessage(), e1);
			throw exc;
		}
	}

	
}
