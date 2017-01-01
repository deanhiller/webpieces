package com.webpieces.http2engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.Http2DataPayload;
import com.webpieces.http2engine.api.Http2FullHeaders;
import com.webpieces.http2engine.api.Http2FullPushPromise;
import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
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
		this.decoder = new HeaderDecoding(localSettings);
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
			Http2DataPayload data = new Http2DataPayload(f);
			incomingData(data);
			return;
		}
		
		int streamId = lowLevelFrame.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("Incoming control frame did not have stream id==0.  frame="+lowLevelFrame);
		
		if(lowLevelFrame instanceof SettingsFrame) {
			SettingsFrame settings = (SettingsFrame) lowLevelFrame;
			processHttp2SettingsFrame(settings);
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
			Http2FullHeaders fullHeaderPayload = new Http2FullHeaders();
			fullHeaderPayload.setStreamId(first.getStreamId());
			fullHeaderPayload.setPriorityDetails(f.getPriorityDetails());
			fullHeaderPayload.setHeaderList(headers);
			
			nextLayer.sendPayloadToClient(fullHeaderPayload);
		} else if(first instanceof PushPromiseFrame) {
			PushPromiseFrame promise = (PushPromiseFrame) first;
			
			Http2FullPushPromise fullPromise = new Http2FullPushPromise();
			fullPromise.setStreamId(promise.getPromisedStreamId());
			fullPromise.setOriginalStreamId(promise.getStreamId());
			fullPromise.setHeaderList(headers);
			
			nextLayer.sendPushPromiseToClient(fullPromise);
		}
	}
	
	public void incomingData(Http2Payload f) {
		if (f.getStreamId() <= 0) {
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, f.getStreamId(),
					"frame streamId cannot be <= 0");
		}

		log.info("incoming payload="+f);
		nextLayer.sendPayloadToClient(f);
	}

}
