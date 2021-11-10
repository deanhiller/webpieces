package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2.api.dto.error.CancelReasonCode;
import com.webpieces.http2.api.dto.lowlevel.GoAwayFrame;
import com.webpieces.http2.api.dto.lowlevel.PingFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2engine.api.error.ShutdownConnection;
import com.webpieces.http2engine.impl.shared.data.HeaderSettings;

public class Level7MarshalAndPing {

	private static final Logger log = LoggerFactory.getLogger(Level7MarshalAndPing.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private HpackParser parser;
	private EngineResultListener finalLayer;
	private HeaderSettings remoteSettings;
	private MarshalState marshalState;
	private AtomicReference<XFuture<Void>> pingFutureRef;
	private String key;
	
	public Level7MarshalAndPing(String key, HpackParser parser, HeaderSettings remoteSettings, EngineResultListener finalLayer) {
		this.key = key;
		this.parser = parser;
		this.remoteSettings = remoteSettings;
		this.finalLayer = finalLayer;
		
		this.remoteSettings = remoteSettings;
        marshalState = parser.prepareToMarshal(remoteSettings.getHeaderTableSize(), remoteSettings.getMaxFrameSize());
	}
	
//	public XFuture<Void> sendControlFrameToClient(Http2Msg msg) {
//		return finalLayer.sendControlFrameToClient(msg);
//	}
	
	public XFuture<Void> sendPing() {
		PingFrame ping = new PingFrame();
		ping.setOpaqueData(8L);

		XFuture<Void> newFuture = new XFuture<>();
		boolean wasSet = pingFutureRef.compareAndSet(null, newFuture);
		if(!wasSet) {
			throw new IllegalStateException(key+"You must wait until the first ping you sent is complete.  2nd ping="+ping);
		}

		return sendFrameToSocket(ping)
				.thenCompose(c -> newFuture);
	}
	
	public XFuture<Void> processPing(PingFrame ping) {
		if(!ping.isPingResponse()) {
			PingFrame pingAck = new PingFrame();
			pingAck.setIsPingResponse(true);
			pingAck.setOpaqueData(ping.getOpaqueData());
			return sendFrameToSocket(pingAck);
		}

		XFuture<Void> future = pingFutureRef.get();
		if(future == null)
			throw new IllegalStateException(key+"bug, this should not be possible");

		pingFutureRef.compareAndSet(future, null); //clear the value
		future.complete(null);
		
		return XFuture.completedFuture(null);
	}

	public void setEncoderMaxTableSize(int value) {
		remoteSettings.setHeaderTableSize(value);
		marshalState.setOutgoingMaxTableSize(value);
	}
	
	public XFuture<Void> goAway(ShutdownConnection shutdown) {
		CancelReasonCode reason = shutdown.getReasonCode();
		byte[] bytes = shutdown.getReason().getBytes(StandardCharsets.UTF_8);
		DataWrapper debug = dataGen.wrapByteArray(bytes);

		GoAwayFrame frame = new GoAwayFrame();
		frame.setDebugData(debug);
		frame.setKnownErrorCode(reason.getErrorCode());

		XFuture<Void> future1 = sendControlDataToSocket(frame);
		finalLayer.closeSocket(shutdown);
		return future1;
	}
	
	public XFuture<Void> sendControlDataToSocket(Http2Msg msg) {
		int streamId = msg.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+msg.getClass());

		return sendFrameToSocket(msg);
	}

	public XFuture<Void> sendFrameToSocket(Http2Msg msg) {
		if(log.isDebugEnabled())
			log.debug(key+"sending frame down to socket(from client)=\n"+msg);
		DataWrapper data = parser.marshal(marshalState, msg);
		ByteBuffer buffer = ByteBuffer.wrap(data.createByteArray());
		return sendToSocket(buffer);
	}

	public XFuture<Void> sendToSocket(ByteBuffer buffer) {
		return finalLayer.sendToSocket(buffer);
	}


}
