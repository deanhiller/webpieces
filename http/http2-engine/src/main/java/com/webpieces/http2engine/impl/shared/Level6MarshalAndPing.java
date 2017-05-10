package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level6MarshalAndPing {

	private static final Logger log = LoggerFactory.getLogger(Level6MarshalAndPing.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

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
	
	public CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg) {
		return finalLayer.sendControlFrameToClient(msg);
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
	
	public CompletableFuture<Void> processPing(PingFrame ping) {
		if(!ping.isPingResponse()) {
			PingFrame pingAck = new PingFrame();
			pingAck.setIsPingResponse(true);
			return sendFrameToSocket(pingAck);
		}

		CompletableFuture<Void> future = pingFutureRef.get();
		if(future == null)
			throw new IllegalStateException("bug, this should not be possible");

		pingFutureRef.compareAndSet(future, null); //clear the value
		future.complete(null);
		
		return CompletableFuture.completedFuture(null);
	}

	public void setEncoderMaxTableSize(int value) {
		remoteSettings.setHeaderTableSize(value);
		marshalState.setOutgoingMaxTableSize(value);
	}
	
	public CompletableFuture<Void> goAway(Http2Exception e) {
		ParseFailReason reason = e.getReason();
		byte[] bytes = e.getMessage().getBytes(StandardCharsets.UTF_8);
		DataWrapper debug = dataGen.wrapByteArray(bytes);

		GoAwayFrame frame = new GoAwayFrame();
		frame.setDebugData(debug);
		frame.setKnownErrorCode(reason.getErrorCode());
		
		CompletableFuture<Void> future1 = sendControlDataToSocket(frame);
		finalLayer.closeSocket(e);
		return future1;
	}
	
	public CompletableFuture<Void> sendControlDataToSocket(Http2Msg msg) {
		int streamId = msg.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+msg.getClass());

		return sendFrameToSocket(msg);
	}

	public CompletableFuture<Void> sendFrameToSocket(Http2Msg msg) {
		log.info("sending frame down to socket(from client)=\n"+msg);
		DataWrapper data = parser.marshal(marshalState, msg);
		ByteBuffer buffer = ByteBuffer.wrap(data.createByteArray());
		return sendToSocket(buffer);
	}

	public CompletableFuture<Void> sendToSocket(ByteBuffer buffer) {
		return finalLayer.sendToSocket(buffer);
	}

}
