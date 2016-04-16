package org.webpieces.nio.impl.cm.secure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.libs.AsyncSSLEngine;
import org.webpieces.nio.api.libs.PacketAction;


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

	public void incomingData(Channel c, ByteBuffer chunk) throws IOException {
		if(!isClosed) {
			PacketAction action = handler.feedEncryptedPacket(chunk, null);
		} else {
			chunk.position(chunk.limit()); //if closed, read the data so we don't get warnings
		}
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
