package com.webpieces.http2engine.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Encoder;
import com.webpieces.http2engine.api.Http2FullHeaders;
import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2HeadersFrame;
import com.webpieces.http2parser.api.dto.Http2Settings;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Level3FlowControl {

	private Http2Parser2 lowLevelParser;
	private ResultListener socketListener;
	private HeaderEncoding encoding;
	private HeaderSettings remoteSettings;

	public Level3FlowControl(Http2Parser2 lowLevelParser, ResultListener socketListener, HeaderSettings remoteSettings) {
		this.lowLevelParser = lowLevelParser;
		this.socketListener = socketListener;
		this.remoteSettings = remoteSettings;
        Encoder encoder = new Encoder(remoteSettings.getHeaderTableSize());
        encoding = new HeaderEncoding(encoder);
	}

	public CompletableFuture<Void> marshalControlFrame(Http2Payload payload) {
		if(payload instanceof Http2Settings) {
			AbstractHttp2Frame frame = (AbstractHttp2Frame) payload;
			DataWrapper data = lowLevelParser.marshal(frame);
			ByteBuffer frameBytes = translate(data);
			return socketListener.sendToSocket(frameBytes);
		} else
			throw new IllegalArgumentException("control frame not supported"+payload.getClass());
	}

	public CompletableFuture<Void> sendFrame(Http2Payload payload) {
		if(payload instanceof Http2Data) {
			Http2Data frame = (Http2Data) payload;
			DataWrapper data = lowLevelParser.marshal(frame);
			ByteBuffer frameBytes = translate(data);
			return socketListener.sendToSocket(frameBytes);
		} else if(payload instanceof Http2FullHeaders) {
			Http2FullHeaders headers = (Http2FullHeaders) payload;
			
	    	Http2HeadersFrame frame = new Http2HeadersFrame();
	    	frame.setStreamId(headers.getStreamId());
	    	return encodeAndSend(frame, headers.getHeaderList());
		}
		
		return null;
	}

	private CompletableFuture<Void> encodeAndSend(HasHeaderFragment initialFrame, List<Http2Header> headers) {
		List<Http2Frame> framesToSend = encoding.createHeaderFrames(initialFrame, headers, remoteSettings.getMaxFrameSize());
		CompletableFuture<Void> lastFuture = null;
		for(Http2Frame frame : framesToSend) {
			DataWrapper data = lowLevelParser.marshal(frame);
			ByteBuffer frameBug = translate(data);
			lastFuture = socketListener.sendToSocket(frameBug);
		}
		return lastFuture;
	}

	public CompletableFuture<Void> sendInitialization(ByteBuffer preface, Http2Settings settings) {
		DataWrapper data = lowLevelParser.marshal(settings);
		ByteBuffer settingsBytes = translate(data);
		
		return socketListener.sendToSocket(preface)
				.thenCompose(c -> socketListener.sendToSocket(settingsBytes));
	}

	private ByteBuffer translate(DataWrapper data) {
		return ByteBuffer.wrap(data.createByteArray());
	}

	public void fireControlFrame(Http2Frame lowLevelFrame) {
		throw new UnsupportedOperationException("not done yet");
	}

}
