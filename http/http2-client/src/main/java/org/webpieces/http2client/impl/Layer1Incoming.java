package org.webpieces.http2client.impl;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.exceptions.SneakyThrow;

import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2engine.api.client.Http2ClientEngine;

public class Layer1Incoming implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Layer1Incoming.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2ClientEngine layer2;
	private Http2SocketListener socketListener;
	private Http2Socket socket;

	public Layer1Incoming(Http2ClientEngine layer2, Http2SocketListener socketListener, Http2Socket socket) {
		this.layer2 = layer2;
		this.socketListener = socketListener;
		this.socket = socket;
	}

	public XFuture<Void> sendInitialFrames() {
		return layer2.sendInitializationToSocket();
	}
	
	public XFuture<Void> sendPing() {
		return layer2.sendPing();
	}
	
	public RequestStreamHandle openStream() {
		return layer2.openStream();
	}

	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		if(log.isDebugEnabled())
			log.debug(channel+"incoming data. size="+b.remaining());
		DataWrapper data = dataGen.wrapByteBuffer(b);
		//log.info("data="+data.createStringFrom(0, data.getReadableSize(), StandardCharsets.UTF_8));
		return layer2.parse(data);
	}

	@Override
	public void farEndClosed(Channel channel) {
		try {
			layer2.farEndClosed();
			socketListener.socketFarEndClosed(socket);
		} catch(Throwable e) {
			try {
				socketListener.socketFarEndClosed(socket);
			} catch(Throwable t) {
				e.addSuppressed(t);
				throw SneakyThrow.sneak(e);
			}
		}
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warn("failure", e);
	}

}
