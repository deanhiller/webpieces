package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level6MarshalAndPing implements EngineResultListener {

	private static final Logger log = LoggerFactory.getLogger(Level6MarshalAndPing.class);
	
	private HpackParser parser;
	private EngineResultListener finalLayer;
	private HeaderSettings remoteSettings;
	private MarshalState marshalState;
	private AtomicReference<CompletableFuture<Void>> pingFutureRef;
	
	public Level6MarshalAndPing(HpackParser parser, HeaderSettings remoteSettings, EngineResultListener finalLayer) {
		this.parser = parser;
		this.remoteSettings = remoteSettings;
		this.finalLayer = finalLayer;
		
		this.remoteSettings = remoteSettings;
        marshalState = parser.prepareToMarshal(remoteSettings.getHeaderTableSize(), remoteSettings.getMaxFrameSize());
	}
	
	@Override
	public void sendControlFrameToClient(Http2Msg msg) {
		finalLayer.sendControlFrameToClient(msg);
	}
	
	public CompletableFuture<Void> sendPing() {
		PingFrame ping = new PingFrame();

		CompletableFuture<Void> newFuture = new CompletableFuture<>();
		boolean wasSet = pingFutureRef.compareAndSet(null, newFuture);
		if(!wasSet) {
			throw new IllegalStateException("You must wait until the first ping you sent is complete.  2nd ping="+ping);
		}

		return sendFrameToSocket(ping)
				.thenCompose(c -> newFuture);
	}
	
	public void processPing(PingFrame ping) {
		if(!ping.isPingResponse()) {
			PingFrame pingAck = new PingFrame();
			pingAck.setIsPingResponse(true);
			sendFrameToSocket(pingAck);
			return;
		}

		CompletableFuture<Void> future = pingFutureRef.get();
		if(future == null)
			throw new IllegalStateException("bug, this should not be possible");

		pingFutureRef.compareAndSet(future, null); //clear the value
		future.complete(null);
	}

	public void setEncoderMaxTableSize(int value) {
		remoteSettings.setHeaderTableSize(value);
		marshalState.setOutgoingMaxTableSize(value);
	}
	
	
	public CompletableFuture<Void> sendControlDataToSocket(Http2Msg msg) {
		int streamId = msg.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+msg.getClass());

		return sendFrameToSocket(msg);
	}

	public CompletableFuture<Void> sendFrameToSocket(Http2Msg msg) {
		log.info("sending frame down to socket(from client)="+msg);
		DataWrapper data = parser.marshal(marshalState, msg);
		ByteBuffer buffer = ByteBuffer.wrap(data.createByteArray());
		return sendToSocket(buffer);
	}
	
	@Override
	public void farEndClosed() {
		finalLayer.farEndClosed();
	}

	@Override
	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return finalLayer.sendToSocket(buffer);
	}

}
