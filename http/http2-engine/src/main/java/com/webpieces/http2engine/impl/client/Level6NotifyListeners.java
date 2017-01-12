package com.webpieces.http2engine.impl.client;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.http2engine.api.client.ClientEngineListener;
import com.webpieces.http2engine.impl.shared.HeaderSettings;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class Level6NotifyListeners {

	private static final Logger log = LoggerFactory.getLogger(Level6NotifyListeners.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private HpackParser parser;
	private ClientEngineListener resultListener;
	private HeaderSettings remoteSettings;
	private MarshalState marshalState;
	private AtomicReference<CompletableFuture<Void>> pingFutureRef;

	public Level6NotifyListeners(HpackParser parser, ClientEngineListener socketListener, HeaderSettings remoteSettings) {
		this.parser = parser;
		this.resultListener = socketListener;
		this.remoteSettings = remoteSettings;
        marshalState = parser.prepareToMarshal(remoteSettings.getHeaderTableSize(), remoteSettings.getMaxFrameSize());
	}

	public CompletableFuture<Void> sendClearTextUpgrade(byte[] bytes) {
		log.info("send cleartext upgrade");
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return resultListener.sendToSocket(buffer).thenApply(c -> null);
	}
	
	public CompletableFuture<Void> sendInitDataToSocket(DataWrapper preface, SettingsFrame settings) {
		log.info("send preface AND settings to socket="+settings);
		DataWrapper settingsData = parser.marshal(marshalState, settings);
		DataWrapper allData = dataGen.chainDataWrappers(preface, settingsData);
		ByteBuffer buffer = translate(allData);
		return resultListener.sendToSocket(buffer).thenApply(c -> null);
	}
	
	public void sendControlFrameToClient(Http2Msg msg) {
		if(msg instanceof GoAwayFrame) {
			resultListener.sendControlFrameToClient((Http2Frame) msg);
		} else
			throw new UnsupportedOperationException("not done yet. frame="+msg);
	}
	
	public CompletableFuture<Void> sendControlDataToSocket(Http2Msg msg) {
		int streamId = msg.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+msg.getClass());

		return sendFrameToSocket(msg);
	}
	
	public CompletableFuture<Void> sendPing(PingFrame ping) {
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
	
	public CompletableFuture<Void> sendFrameToSocket(Http2Msg msg) {
		log.info("sending frame down to socket(from client)="+msg);
		DataWrapper data = parser.marshal(marshalState, msg);
		ByteBuffer buffer = translate(data);
		return resultListener.sendToSocket(buffer);
	}
	
	private ByteBuffer translate(DataWrapper data) {
		return ByteBuffer.wrap(data.createByteArray());
	}
	
	public void farEndClosed() {
		this.resultListener.engineClosedByFarEnd();
	}

	public void setEncoderMaxTableSize(int value) {
		remoteSettings.setHeaderTableSize(value);
		marshalState.setOutgoingMaxTableSize(value);
	}
}
