package com.webpieces.http2engine.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.Http2ClientEngine;
import com.webpieces.http2engine.api.PartialStream;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.dto.SettingsFrame;

public class Level0ClientEngine implements Http2ClientEngine {
	
	private static final byte[] preface = DatatypeConverter.parseHexBinary("505249202a20485454502f322e300d0a0d0a534d0d0a0d0a");
	
	private Level2AggregateDecodeHeaders headers;
	private Level5FlowControl flowControlLevel5;
	private Level1IncomingParsing parsingLayer;
	private Level3StreamInitialization streamInitializationLevel3;

	public Level0ClientEngine(String id, Http2Parser2 lowLevelParser, ResultListener socketListener) {
		HeaderSettings remoteSettings = new HeaderSettings();
		HeaderSettings localSettings = new HeaderSettings();

		flowControlLevel5 = new Level5FlowControl(lowLevelParser, socketListener, remoteSettings);
		Level4ClientStateMachine clientSm = new Level4ClientStateMachine(id, flowControlLevel5);
		streamInitializationLevel3 = new Level3StreamInitialization(clientSm);
		headers = new Level2AggregateDecodeHeaders(streamInitializationLevel3, flowControlLevel5, localSettings);
		parsingLayer = new Level1IncomingParsing(headers, lowLevelParser);
	}

	@Override
	public CompletableFuture<Void> sendFrameToSocket(PartialStream frame) {
		int streamId = frame.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException("frames for requests must have a streamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");

		return streamInitializationLevel3.outgoingFrame(frame);
	}

	@Override
	public void cancel(int streamId) {
		throw new UnsupportedOperationException("not supported yet");
	}
	
	/**
	 * NOT thread-safe.  This is meant to be called from a 'virtual' single thread or a single thread.
	 * channelmanager2 uses 'virtual' single threads ensuring order and never running at the same time
	 * but it may not always be the same thread either(to avoid starvation)
	 */


	@Override
	public CompletableFuture<Void> sendInitializationToSocket() {
		ByteBuffer buf = ByteBuffer.wrap(preface);
		SettingsFrame settings = new SettingsFrame();
		return flowControlLevel5.sendInitDataToSocket(buf, settings);
	}

	@Override
	public void parse(DataWrapper newData) {
		parsingLayer.parse(newData);
	}

	@Override
	public void closeEngine() {
		flowControlLevel5.closeEngine();
	}

}
