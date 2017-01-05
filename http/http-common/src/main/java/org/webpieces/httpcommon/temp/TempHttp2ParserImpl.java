package org.webpieces.httpcommon.temp;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.Http2FullHeaders;
import org.webpieces.httpcommon.api.Http2FullPushPromise;

import com.twitter.hpack.Decoder;
import com.webpieces.http2engine.impl.HeaderDecoding;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;
import com.webpieces.http2parser.api.dto.lib.SettingsParameter;

public class TempHttp2ParserImpl implements TempHttp2Parser {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2Parser parser;

	public Http2Memento prepareToParse() {
		Http2Memento result = parser.prepareToParse();
		return new ParserResult2Impl(result);
	}

	public Http2Memento parse(Http2Memento oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings) {
		ParserResult2Impl state = (ParserResult2Impl) oldData;
		state.getParsedFrames().clear(); //reset any parsed frames
		
		long maxFrameSize = fetchMaxFrameSize(settings);
		Http2Memento result = parser.parse(state.getResult(), newData, maxFrameSize);
		
		List<Http2Frame> parsedFrames = result.getParsedFrames();
		for(Http2Frame frame: parsedFrames) {
			processFrame(state, frame, decoder);
		}
		
		return state;
	}

	private void processFrame(ParserResult2Impl state, Http2Frame frame, Decoder decoder) {
		List<HasHeaderFragment> headerFragList = state.getHasHeaderFragmentList();
		if(frame instanceof HasHeaderFragment) {
			HasHeaderFragment headerFrame = (HasHeaderFragment) frame;
			headerFragList.add(headerFrame);
			validateHeader(state, headerFrame);
			if(headerFrame.isEndHeaders())
				combineAndSendHeadersToClient(state, decoder);
			return;
		} else if(headerFragList.size() > 0) {
			throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, frame.getStreamId(), 
					"Parser in the middle of accepting headers(spec "
					+ "doesn't allow frames between header fragments).  frame="+frame+" list="+headerFragList, true);
		}
		
		if(!(frame instanceof UnknownFrame)) 
			state.getParsedFrames().add(frame);
	}

	private void validateHeader(ParserResult2Impl state, HasHeaderFragment lowLevelFrame) {
		List<HasHeaderFragment> list = state.getHasHeaderFragmentList();
		HasHeaderFragment first = list.get(0);
		int streamId = first.getStreamId();
		if(first instanceof PushPromiseFrame) {
			PushPromiseFrame f = (PushPromiseFrame) first;
			streamId = f.getPromisedStreamId();
		}
		
		
		if(list.size() == 1) {
			if(!(first instanceof HeadersFrame) && !(first instanceof PushPromiseFrame))
				throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, lowLevelFrame.getStreamId(), 
						"First has header frame must be HeadersFrame or PushPromiseFrame first frame="+first, true);				
		} else if(streamId != lowLevelFrame.getStreamId()) {
			throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, lowLevelFrame.getStreamId(), 
					"Headers/continuations from two different streams per spec cannot be"
					+ " interleaved.  frames="+list, true);
		} else if(!(lowLevelFrame instanceof ContinuationFrame)) {
			throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, lowLevelFrame.getStreamId(), 
					"Must be continuation frame and wasn't.  frames="+list, true);			
		}
	}
	
	private long fetchMaxFrameSize(List<Http2Setting> settings) {
		for(Http2Setting s : settings) {
			if(s.getKnownName() == SettingsParameter.SETTINGS_MAX_FRAME_SIZE)
				return s.getValue();
		}
		
		int defaultMaxFrameSize = 16_384; 

		//otherwise return default
		return defaultMaxFrameSize;
	}
	
	private void combineAndSendHeadersToClient(ParserResult2Impl state, Decoder decoder) {
		List<HasHeaderFragment> hasHeaderFragmentList = state.getHasHeaderFragmentList();
		// Now we set the full header list on the first frame and just return that
		HasHeaderFragment firstFrame = hasHeaderFragmentList.get(0);
		DataWrapper allSerializedHeaders = dataGen.emptyWrapper();
		for (HasHeaderFragment iterFrame : hasHeaderFragmentList) {
		    allSerializedHeaders = dataGen.chainDataWrappers(allSerializedHeaders, iterFrame.getHeaderFragment());
		}
		
		HeaderDecoding decoding = new HeaderDecoding(decoder);
		List<Http2Header> headers = decoding.decode(allSerializedHeaders);

		//TODO: fix this to remove HeadersFrame and PushPromieFrame from having a List<Header> field
		if(firstFrame instanceof HeadersFrame) {
			HeadersFrame f = (HeadersFrame) firstFrame;
			Http2FullHeaders full = new Http2FullHeaders(f.getStreamId(), headers, f.getPriorityDetails(), f.isEndStream());
			state.getParsedFrames().add(full);
		} else if(firstFrame instanceof PushPromiseFrame) {
			PushPromiseFrame f = (PushPromiseFrame) firstFrame;
			Http2FullPushPromise promise = new Http2FullPushPromise(
					headers, f.getPromisedStreamId(), f.getStreamId());
			state.getParsedFrames().add(promise);
		}

		hasHeaderFragmentList.clear();
	}

	public DataWrapper marshal(Http2Frame frame) {
		return parser.marshal(frame);
	}

	public SettingsFrame unmarshalSettingsPayload(ByteBuffer settingsPayload) {
		return parser.unmarshalSettingsPayload(settingsPayload);
	}

	public TempHttp2ParserImpl(Http2Parser parser) {
		this.parser = parser;
	}

}
