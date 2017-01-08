package com.webpieces.hpack.impl;

import java.io.IOException;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class HpackParserImpl implements HpackParser {

	private static final Logger log = LoggerFactory.getLogger(HpackParserImpl.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HeaderEncoding encoding = new HeaderEncoding();
	private HeaderDecoding decoding = new HeaderDecoding();
	private Http2Parser parser;
	private boolean ignoreUnkownFrames;

	public HpackParserImpl(Http2Parser parser, boolean ignoreUnkownFrames) {
		this.parser = parser;
		this.ignoreUnkownFrames = ignoreUnkownFrames;
	}

	@Override
	public void setDecoderMaxTableSize(UnmarshalState memento, int newSize) {
		HpackMementoImpl state = (HpackMementoImpl) memento;
		decoding.setMaxHeaderTableSize(state, newSize);
	}
	
	@Override
	public UnmarshalState prepareToUnmarshal(int maxHeaderSize, int maxHeaderTableSize) {
		Decoder decoder = new Decoder(maxHeaderSize, maxHeaderTableSize);
		Http2Memento result = parser.prepareToParse();
		return new HpackMementoImpl(result, decoder);
	}

	@Override
	public UnmarshalState unmarshal(UnmarshalState memento, DataWrapper newData, long maxFrameSize) {
		HpackMementoImpl state = (HpackMementoImpl) memento;
		state.getParsedFrames().clear(); //reset any parsed frames
		
		Http2Memento result = parser.parse(state.getLowLevelState(), newData, maxFrameSize);
		
		List<Http2Frame> parsedFrames = result.getParsedFrames();
		for(Http2Frame frame: parsedFrames) {
			processFrame(state, frame);
		}
		
		return state;
	}

	private void processFrame(HpackMementoImpl state, Http2Frame frame) {
		List<HasHeaderFragment> headerFragList = state.getHeadersToCombine();
		if(frame instanceof HasHeaderFragment) {
			HasHeaderFragment headerFrame = (HasHeaderFragment) frame;
			headerFragList.add(headerFrame);
			validateHeader(state, headerFrame);
			if(headerFrame.isEndHeaders())
				combineAndSendHeadersToClient(state);
			return;
		} else if(headerFragList.size() > 0) {
			throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR, frame.getStreamId(), 
					"Parser in the middle of accepting headers(spec "
					+ "doesn't allow frames between header fragments).  frame="+frame+" list="+headerFragList, true);
		}
		
		if(frame instanceof UnknownFrame && ignoreUnkownFrames) {
			//do nothing
		} else if(frame instanceof Http2Msg) {
			state.getParsedFrames().add((Http2Msg) frame);
		} else {
			throw new IllegalStateException("bug forgot support for frame="+frame);
		}
	}

	private void validateHeader(HpackMementoImpl state, HasHeaderFragment lowLevelFrame) {
		List<HasHeaderFragment> list = state.getHeadersToCombine();
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
	
	private void combineAndSendHeadersToClient(HpackMementoImpl state) {
		List<HasHeaderFragment> hasHeaderFragmentList = state.getHeadersToCombine();
		// Now we set the full header list on the first frame and just return that
		HasHeaderFragment firstFrame = hasHeaderFragmentList.get(0);
		DataWrapper allSerializedHeaders = dataGen.emptyWrapper();
		for (HasHeaderFragment iterFrame : hasHeaderFragmentList) {
		    allSerializedHeaders = dataGen.chainDataWrappers(allSerializedHeaders, iterFrame.getHeaderFragment());
		}
		
		List<Http2Header> headers = decoding.decode(state.getDecoder(), allSerializedHeaders);

		if(firstFrame instanceof HeadersFrame) {
			HeadersFrame f = (HeadersFrame) firstFrame;
			Http2Headers fullHeaders = new Http2Headers(headers);
			fullHeaders.setStreamId(f.getStreamId());
			fullHeaders.setPriorityDetails(f.getPriorityDetails());
			fullHeaders.setEndOfStream(f.isEndOfStream());
			state.getParsedFrames().add(fullHeaders);
		} else if(firstFrame instanceof PushPromiseFrame) {
			PushPromiseFrame f = (PushPromiseFrame) firstFrame;
			Http2Push fullHeaders = new Http2Push(headers);
			fullHeaders.setStreamId(f.getStreamId());
			fullHeaders.setPromisedStreamId(f.getPromisedStreamId());
			state.getParsedFrames().add(fullHeaders);
		}

		hasHeaderFragmentList.clear();
	}

	@Override
    public MarshalState prepareToMarshal(int maxHeaderTableSize) {
		Encoder encoder = new Encoder(maxHeaderTableSize);
		return new MarshalStateImpl(encoder);
	}

	@Override
	public DataWrapper marshal(MarshalState memento, Http2Msg msg, long maxFrameSize) {
		MarshalStateImpl state = (MarshalStateImpl) memento;
		if(msg instanceof Http2Headers) {
			Http2Headers h = (Http2Headers) msg;
			return createHeadersData(state, h, maxFrameSize);
		} else if(msg instanceof Http2Push) {
			Http2Push p = (Http2Push) msg;
			return createPushPromiseData(state, p, maxFrameSize);
		} else if(msg instanceof Http2Frame) {
			return parser.marshal((Http2Frame) msg);
		} else
			throw new IllegalStateException("bug, missing case for msg="+msg);
	}

	private DataWrapper createPushPromiseData(MarshalStateImpl state, Http2Push p, long maxFrameSize) {
    	PushPromiseFrame promise = new PushPromiseFrame();
    	promise.setStreamId(p.getStreamId());
    	promise.setPromisedStreamId(p.getPromisedStreamId());
		List<Http2Header> headers = p.getHeaders();
    	
		return marshal(state, maxFrameSize, promise, headers);
	}

	private DataWrapper createHeadersData(MarshalStateImpl state, Http2Headers headers, long maxFrameSize) {
    	HeadersFrame frame = new HeadersFrame();
    	frame.setStreamId(headers.getStreamId());
    	frame.setEndOfStream(headers.isEndOfStream());
    	frame.setPriorityDetails(headers.getPriorityDetails());
    	List<Http2Header> headerList = headers.getHeaders();
		
		return marshal(state, maxFrameSize, frame, headerList);
	}
	
	private DataWrapper marshal(MarshalStateImpl state, long maxFrameSize, HasHeaderFragment promise,
			List<Http2Header> headers) {
		List<Http2Frame> headerFrames = encoding.createHeaderFrames(promise, headers, state.getEncoder(), maxFrameSize);
		
		DataWrapper allData = DataWrapperGeneratorFactory.EMPTY;
		for(Http2Frame f : headerFrames) {
			DataWrapper frameData = parser.marshal(f);
			allData = dataGen.chainDataWrappers(allData, frameData);
		}
		
		return allData;
	}

	@Override
    public void setEncoderMaxTableSize(MarshalState memento, int newSize) {
		MarshalStateImpl state = (MarshalStateImpl) memento;
		try {
			encoding.setMaxHeaderTableSize(state, newSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

	@Override
	public List<Http2Setting> unmarshalSettingsPayload(String base64SettingsPayload) {
		return parser.unmarshalSettingsPayload(base64SettingsPayload);
	}

	@Override
	public String marshalSettingsPayload(List<Http2Setting> settingsPayload) {
		return parser.marshalSettingsPayload(settingsPayload);
	}
}