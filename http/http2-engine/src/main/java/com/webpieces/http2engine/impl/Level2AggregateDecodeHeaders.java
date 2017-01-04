package com.webpieces.http2engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.twitter.hpack.Decoder;
import com.webpieces.http2engine.api.dto.Http2Data;
import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2engine.api.dto.Http2Push;
import com.webpieces.http2engine.api.dto.Http2UnknownFrame;
import com.webpieces.http2engine.api.dto.PartialStream;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PingFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Level2AggregateDecodeHeaders {

	private static final Logger log = LoggerFactory.getLogger(Level2AggregateDecodeHeaders.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private Level3StreamInitialization nextLayer;
	private Level5FlowControl flowControlLevel5;
	private HeaderDecoding decoder;

	//The only state in this class
	private List<HasHeaderFragment> accumulatingHeaders = new ArrayList<>();

	public Level2AggregateDecodeHeaders(Level3StreamInitialization nextLayer, Level5FlowControl flowControlLevel5, HeaderSettings localSettings) {
		this.nextLayer = nextLayer;
		this.flowControlLevel5 = flowControlLevel5;
        Decoder decode = new Decoder(16_384, localSettings.getMaxHeaderTableSize());
		this.decoder = new HeaderDecoding(decode);
	}

	/************************************************************************
	 * Incoming data path only below here
	 *************************************************************************/

	public void sendFrameUpToClient(Http2Frame lowLevelFrame) {
		log.info("frame from socket="+lowLevelFrame);
		//headers on two streams must all come in series for stream 1 then stream 2 per spec.  only
		//data frames can be multiplexed per spec
		if(lowLevelFrame instanceof HasHeaderFragment) {
			HasHeaderFragment headerFrame = (HasHeaderFragment) lowLevelFrame;
			accumulatingHeaders.add(headerFrame);
			validateHeader(lowLevelFrame);
			if(headerFrame.isEndHeaders())
				combineAndSendHeadersToClient();
			return;
		} else if(accumulatingHeaders.size() > 0) {
			throw new IllegalArgumentException("HasHeaderFragments are required to be consecutive per spec but somehow are not.  accumulatedFrames="+accumulatingHeaders);
		} else if(lowLevelFrame instanceof DataFrame) {
			DataFrame f = (DataFrame) lowLevelFrame;
			Http2Data data = new Http2Data(f.getStreamId(), f.isEndStream(), f.getData());
			incomingData(data);
			return;
		} else if(lowLevelFrame instanceof UnknownFrame) {
			UnknownFrame f = (UnknownFrame) lowLevelFrame;
			Http2UnknownFrame unknown = new Http2UnknownFrame();
			unknown.setFlagsByte(f.getFlagsByte());
			unknown.setFramePayloadData(f.getFramePayloadData());
			unknown.setFrameTypeId(f.getFrameTypeId());
			unknown.setStreamId(f.getStreamId());
			incomingData(unknown);
		} else if(lowLevelFrame instanceof SettingsFrame) {
			SettingsFrame settings = (SettingsFrame) lowLevelFrame;
			processHttp2SettingsFrame(settings);
		} else if(lowLevelFrame instanceof PingFrame) {
			flowControlLevel5.processPing((PingFrame)lowLevelFrame);
		} else {
			flowControlLevel5.sendControlFrameToClient(lowLevelFrame);
		}
	}

	private void processHttp2SettingsFrame(SettingsFrame settings) {
		if(settings.isAck()) {
			log.info("server acked our settings frame");
		} else {
			log.info("applying remote settings frame");
			flowControlLevel5.applyRemoteSettings(settings);
			//now that settings is applied, ack the settings
			SettingsFrame settingsAck = new SettingsFrame(true);
			
			log.info("sending remote settings ack frame");
			flowControlLevel5.sendControlDataToSocket(settingsAck);
		}
	}

	private void validateHeader(Http2Frame lowLevelFrame) {
		HasHeaderFragment first = accumulatingHeaders.get(0);
		if(first.getStreamId() != lowLevelFrame.getStreamId())
			throw new IllegalArgumentException("Headers/continuations from two different streams per spec cannot be interleaved.  frames="+accumulatingHeaders);
	}

	private void combineAndSendHeadersToClient() {
		DataWrapper allData = dataGen.emptyWrapper();
		
		for(HasHeaderFragment headerFrame : accumulatingHeaders) {
			allData = dataGen.chainDataWrappers(allData, headerFrame.getHeaderFragment());
		}
		
		HasHeaderFragment first = accumulatingHeaders.get(0);
		accumulatingHeaders.clear(); //clear the headers

		List<Http2Header> headers = decoder.decode(allData);

		if(first instanceof HeadersFrame) {

			HeadersFrame f = (HeadersFrame) first;
			Http2Headers fullHeaderPayload = new Http2Headers(headers);
			fullHeaderPayload.setStreamId(first.getStreamId());
			fullHeaderPayload.setPriorityDetails(f.getPriorityDetails());
			fullHeaderPayload.setEndOfStream(f.isEndStream());
			
			nextLayer.sendPayloadToClient(fullHeaderPayload);
		} else if(first instanceof PushPromiseFrame) {
			PushPromiseFrame promise = (PushPromiseFrame) first;
			
			Http2Push fullPromise = new Http2Push(promise.getHeaderList());
			//These are reveresed on purpose and we name it 'causal' meaning
			//the stream that causes this new stream to open
			fullPromise.setStreamId(promise.getPromisedStreamId());
			fullPromise.setCausalStreamId(promise.getStreamId());
			
			nextLayer.sendPushPromiseToClient(fullPromise);
		} else
			throw new IllegalStateException("bug, should not be able to end up here");
	}
	
	public void incomingData(PartialStream f) {
		if (f.getStreamId() <= 0) {
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, f.getStreamId(),
					"frame streamId cannot be <= 0", true);
		}

		log.info("incoming payload="+f);
		nextLayer.sendPayloadToClient(f);
	}

}
