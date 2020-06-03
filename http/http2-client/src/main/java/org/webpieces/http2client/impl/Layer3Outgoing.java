package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.api.error.ShutdownConnection;

public class Layer3Outgoing implements ClientEngineListener {
	
	private static final Logger log = LoggerFactory.getLogger(Layer3Outgoing.class);
	private TCPChannel channel;
	
	public Layer3Outgoing(TCPChannel channel, Http2Socket socket) {
		this.channel = channel;
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer data) {
		if(log.isTraceEnabled())
			log.trace(channel+"writing out data to socket size="+data.remaining());
		return channel.write(data)
						.thenApply(c -> null);
	}

	public void sendPreface(ByteBuffer buf) {
		channel.write(buf);
	}

	public CompletableFuture<Void> connect(InetSocketAddress addr, Layer1Incoming incoming) {
		return channel.connect(addr, incoming);
	}

	public CompletableFuture<Void> close() {
		return channel.close();
	}

	@Override
	public void sendControlFrameToClient(Http2Frame lowLevelFrame) {
	}

	@Override
	public void engineClosedByFarEnd() {
	}
	
	@Override
	public void closeSocket(ShutdownConnection reason) {
		channel.close();
	}
}
