package com.webpieces.http2engine.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2engine.api.Http2FullHeaders;
import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2engine.api.Http2ClientEngine;
import com.webpieces.http2engine.api.ResultListener;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.Http2Data;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2HeadersFrame;
import com.webpieces.http2parser.api.dto.Http2PushPromise;
import com.webpieces.http2parser.api.dto.Http2Settings;

public class Level0ClientEngine implements Http2ClientEngine {
	
	private static final byte[] preface = DatatypeConverter.parseHexBinary("505249202a20485454502f322e300d0a0d0a534d0d0a0d0a");
	
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Level1AggregateHeaders headers;
	private Level3FlowControl flowControl;
	private Http2Parser2 lowLevelParser;
	
	private Http2Memento parsingState;

	private List<HasHeaderFragment> accumulatingHeaders = new ArrayList<>();

	public Level0ClientEngine(String id, Http2Parser2 lowLevelParser, ResultListener socketListener) {
		HeaderSettings remoteSettings = new HeaderSettings();
		flowControl = new Level3FlowControl(lowLevelParser, socketListener, remoteSettings);
		Level2ClientStateMachine clientSm = new Level2ClientStateMachine(id, flowControl);
		headers = new Level1AggregateHeaders(clientSm, lowLevelParser);
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToParse();
	}

	@Override
	public CompletableFuture<Void> sendFrameOut(Http2Payload frame) {
		int streamId = frame.getStreamId();
		if(streamId <= 0)
			throw new IllegalArgumentException("frames for requests must have a streamId > 0");
		else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");

		return headers.outgoingFrame(frame);
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
	public void parse(DataWrapper newData) {
		parsingState = lowLevelParser.parse(parsingState, newData);
		List<Http2Frame> parsedMessages = parsingState.getParsedMessages();
		
		for(Http2Frame lowLevelFrame : parsedMessages) {
			accummulateAndSendFrames(lowLevelFrame);
		}
	}

	private void accummulateAndSendFrames(Http2Frame lowLevelFrame) {
		//headers on two streams must all come in series for stream 1 then stream 2 per spec.  only
		//data frames can be multiplexed per spec
		if(lowLevelFrame instanceof HasHeaderFragment) {
			HasHeaderFragment headerFrame = (HasHeaderFragment) lowLevelFrame;
			accumulatingHeaders.add(headerFrame);
			if(headerFrame.isEndHeaders())
				combineAndSendHeaders();
			return;
		} else if(accumulatingHeaders.size() > 0) {
			throw new IllegalArgumentException("HasHeaderFragments are required to be consecutive per spec but somehow are not.  accumulatedFrames="+accumulatingHeaders);
		} else if(lowLevelFrame instanceof Http2Data) {
			headers.incomingData((Http2Data) lowLevelFrame);
		}
		
		headers.incomingControlData(lowLevelFrame);
	}

	private void combineAndSendHeaders() {
		DataWrapper allData = dataGen.emptyWrapper();
		
		for(HasHeaderFragment headerFrame : accumulatingHeaders) {
			allData = dataGen.chainDataWrappers(allData, headerFrame.getHeaderFragment());
		}
		
		HasHeaderFragment first = accumulatingHeaders.get(0);
		accumulatingHeaders.clear(); //clear the headers
		
		if(first instanceof Http2HeadersFrame) {
			Http2FullHeaders headers = new Http2FullHeaders();
			
			
		} else if(first instanceof Http2PushPromise) {
		}
		
	}

	@Override
	public CompletableFuture<Void> sendInitialization() {
		ByteBuffer buf = ByteBuffer.wrap(preface);
		Http2Settings settings = new Http2Settings();
		return flowControl.sendInitialization(buf, settings);
	}

}
