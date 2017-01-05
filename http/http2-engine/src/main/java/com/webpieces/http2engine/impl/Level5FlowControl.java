package com.webpieces.http2engine.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.twitter.hpack.Encoder;
import com.webpieces.http2engine.api.EngineListener;
import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2engine.api.dto.PartialStream;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class Level5FlowControl {

	private static final Logger log = LoggerFactory.getLogger(Level5FlowControl.class);
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private Http2Parser lowLevelParser;
	private EngineListener resultListener;
	private HeaderEncoding encoding;
	private HeaderSettings remoteSettings;
	
	private AtomicLong windowSizeLeft;

	private CompletableFuture<Void> pingFuture;

	public Level5FlowControl(
			Http2Parser lowLevelParser, 
			EngineListener socketListener,
			HeaderSettings remoteSettings
	) {
		this.lowLevelParser = lowLevelParser;
		this.resultListener = socketListener;
		this.remoteSettings = remoteSettings;
		Encoder encoder = new Encoder(remoteSettings.getMaxHeaderTableSize());
        encoding = new HeaderEncoding(encoder, remoteSettings.getMaxFrameSize());
        windowSizeLeft = new AtomicLong(remoteSettings.getInitialWindowSize());
	}

	public CompletableFuture<Void> sendControlDataToSocket(Http2Frame frame) {
		int streamId = frame.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+frame.getClass());

		return sendFrameToSocket(frame);
	}
	
	public CompletableFuture<Void> sendPayloadToSocket(PartialStream payload) {
		log.info("sending payload to socket="+payload);
		if(payload instanceof DataFrame) {
			return sendFrameToSocket((DataFrame)payload);
		} else if(payload instanceof Http2Headers) {
			Http2Headers headers = (Http2Headers) payload;
			
	    	HeadersFrame frame = new HeadersFrame();
	    	frame.setStreamId(headers.getStreamId());
	    	frame.setEndStream(headers.isEndOfStream());
	    	frame.setPriorityDetails(headers.getPriorityDetails());
	    	return encodeAndSend(frame, headers.getHeaders(), payload.isEndOfStream());
		} else
			throw new UnsupportedOperationException("not done yet.  frame="+payload);
	}

	private CompletableFuture<Void> encodeAndSend(HasHeaderFragment initialFrame, List<Http2Header> headers, boolean isEndStream) {
		List<Http2Frame> framesToSend = encoding.createHeaderFrames(initialFrame, headers);
		return sendFrameToSocket(framesToSend.toArray(new Http2Frame[0]));
	}

	public CompletableFuture<Void> sendInitDataToSocket(ByteBuffer preface, SettingsFrame settings) {
		log.info("send preface AND settings to socket="+settings);
		return resultListener.sendToSocket(preface)
				.thenCompose(c -> {
					CompletableFuture<Void> socketSend = sendFrameToSocket(settings);
					log.info("sent local settings to remote");
					return socketSend;
				});
	}

	private ByteBuffer translate(DataWrapper data) {
		return ByteBuffer.wrap(data.createByteArray());
	}

	public void sendControlFrameToClient(Http2Frame lowLevelFrame) {
		if(lowLevelFrame instanceof GoAwayFrame) {
			resultListener.sendControlFrameToClient(lowLevelFrame);
		} else
			throw new UnsupportedOperationException("not done yet. frame="+lowLevelFrame);
	}

	/**
	 * return true
	 * @param settings
	 * @return
	 */
	public void applyRemoteSettings(SettingsFrame settings) {
		for(Http2Setting setting : settings.getSettings()) {
			SettingsParameter key = setting.getKnownName();
			if(key == null)
				continue; //unknown setting so skip it and forward to client
			apply(key, setting.getValue());
		}
	}

	private void apply(SettingsParameter key, long value) {
		switch(key) {
			case SETTINGS_HEADER_TABLE_SIZE:
				applyMaxHeaderTableSize(value);
				break;
			case SETTINGS_ENABLE_PUSH:
				applyPushEnabled(value);
				break;
			case SETTINGS_MAX_CONCURRENT_STREAMS:
				remoteSettings.setMaxConcurrentStreams(value);
				break;
			case SETTINGS_INITIAL_WINDOW_SIZE:
				modifyWindowSize(value);
				break;
			case SETTINGS_MAX_FRAME_SIZE:
				applyMaxFrameSize(value);
				break;
			case SETTINGS_MAX_HEADER_LIST_SIZE:
				remoteSettings.setMaxHeaderListSize(value);
				break;
			default:
				throw new RuntimeException("bug, someone forgot to add some new setting="+key+" which had value="+value);
		}
	}

	private void applyMaxFrameSize(long val) {
		int value = convertToInt(val);
		remoteSettings.setMaxFrameSize(value);
		encoding.setMaxFrameSize(value);
	}

	private void applyMaxHeaderTableSize(long val) {
		int value = convertToInt(val);
		remoteSettings.setMaxHeaderTableSize(value);
		encoding.setMaxHeaderTableSize(value);
	}

	private int convertToInt(long value) {
		if(value > Integer.MAX_VALUE)
			throw new RuntimeException("Bug, hpack library only supports up to max integer.  need to modify that library");
		return (int)value;
	}
	
	private void applyPushEnabled(long value) {
		if(value == 0)
			remoteSettings.setPushEnabled(false);
		else if(value == 1)
			remoteSettings.setPushEnabled(true);
		else
			throw new RuntimeException("bug, this should not happen with other preconditions in place");
	}

	private void modifyWindowSize(long initialWindow) {
		long difference = initialWindow - remoteSettings.getInitialWindowSize();
		windowSizeLeft.addAndGet(difference);

		remoteSettings.setInitialWindowSize(initialWindow);
	}

	public void closeEngine() {
		this.resultListener.engineClosedByFarEnd();
	}

	public CompletableFuture<Void> sendPing(PingFrame ping) {
		if(pingFuture != null)
			throw new IllegalStateException("You must wait until the first ping you sent is complete");
		pingFuture = new CompletableFuture<>();
		
		sendFrameToSocket(ping);
		return pingFuture;
	}

	public void processPing(PingFrame ping) {
		if(!ping.isPingResponse()) {
			PingFrame pingAck = new PingFrame();
			pingAck.setIsPingResponse(true);
			sendFrameToSocket(pingAck);
			return;
		}
		
		if(pingFuture == null)
			throw new IllegalStateException("bug, this should not be possible");
		CompletableFuture<Void> tempFuture = pingFuture;
		pingFuture = null; //set to null before firing (in case client throws exception)
		tempFuture.complete(null);
	}

	private CompletableFuture<Void> sendFrameToSocket(Http2Frame ... frames) {
		DataWrapper allFrames = dataGen.emptyWrapper();
		for(Http2Frame frame : frames) {
			log.info("sending frame down to socket(from client)="+frame);
			DataWrapper data = lowLevelParser.marshal(frame);
			allFrames = dataGen.chainDataWrappers(allFrames, data);
		}
		ByteBuffer buffer = translate(allFrames);
		return resultListener.sendToSocket(buffer);
	}
}
