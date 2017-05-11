package com.webpieces.hpack.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.dto.ContinuationFrame;
import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class HpackParserImpl implements HpackParser {

	//private static final Logger log = LoggerFactory.getLogger(HpackParserImpl.class);
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
	public UnmarshalState prepareToUnmarshal(int maxHeaderSize, int maxHeaderTableSize, long localMaxFrameSize) {
		Decoder decoder = new Decoder(maxHeaderSize, maxHeaderTableSize);
		Http2Memento result = parser.prepareToParse(localMaxFrameSize);
		return new UnmarshalStateImpl(result, decoding, decoder);
	}

	@Override
	public UnmarshalState unmarshal(UnmarshalState memento, DataWrapper newData) {
		UnmarshalStateImpl state = (UnmarshalStateImpl) memento;
		state.clearParsedFrames(); //reset any parsed frames
		
		Http2Memento result = parser.parse(state.getLowLevelState(), newData);
		
		List<Http2Frame> parsedFrames = result.getParsedFrames();
		for(Http2Frame frame: parsedFrames) {
			processFrame(state, frame);
		}
		
		return state;
	}

	private void processFrame(UnmarshalStateImpl state, Http2Frame frame) {
		List<HasHeaderFragment> headerFragList = state.getHeadersToCombine();
		if(frame instanceof HasHeaderFragment) {
			HasHeaderFragment headerFrame = (HasHeaderFragment) frame;
			headerFragList.add(headerFrame);
			validateHeader(state, headerFrame);
			if(headerFrame.isEndHeaders())
				combineAndSendHeadersToClient(state);
			return;
		} else if(headerFragList.size() > 0) {			
			throw new ConnectionException(ParseFailReason.HEADERS_MIXED_WITH_FRAMES, frame.getStreamId(), 
					"Parser in the middle of accepting headers(spec "
					+ "doesn't allow frames between header fragments).  frame="+frame+" list="+headerFragList);
		}
		
		if(frame instanceof UnknownFrame && ignoreUnkownFrames) {
			//do nothing
		} else if(frame instanceof Http2Msg) {
			state.getParsedFrames().add((Http2Msg) frame);
		} else {
			throw new IllegalStateException("bug forgot support for frame="+frame);
		}
	}

	private void validateHeader(UnmarshalStateImpl state, HasHeaderFragment lowLevelFrame) {
		List<HasHeaderFragment> list = state.getHeadersToCombine();
		HasHeaderFragment first = list.get(0);
		int streamId = first.getStreamId();
		if(first instanceof PushPromiseFrame) {
			PushPromiseFrame f = (PushPromiseFrame) first;
			streamId = f.getPromisedStreamId();
		}
		
		
		if(list.size() == 1) {
			if(!(first instanceof HeadersFrame) && !(first instanceof PushPromiseFrame))
				throw new ConnectionException(ParseFailReason.HEADERS_MIXED_WITH_FRAMES, lowLevelFrame.getStreamId(), 
						"First has header frame must be HeadersFrame or PushPromiseFrame first frame="+first);				
		} else if(streamId != lowLevelFrame.getStreamId()) {
			throw new ConnectionException(ParseFailReason.HEADERS_MIXED_WITH_FRAMES, lowLevelFrame.getStreamId(), 
					"Headers/continuations from two different streams per spec cannot be"
					+ " interleaved.  frames="+list);
		} else if(!(lowLevelFrame instanceof ContinuationFrame)) {
			throw new ConnectionException(ParseFailReason.HEADERS_MIXED_WITH_FRAMES, lowLevelFrame.getStreamId(), 
					"Must be continuation frame and wasn't.  frames="+list);			
		}
	}
	
	private void combineAndSendHeadersToClient(UnmarshalStateImpl state) {
		List<HasHeaderFragment> hasHeaderFragmentList = state.getHeadersToCombine();
		// Now we set the full header list on the first frame and just return that
		HasHeaderFragment firstFrame = hasHeaderFragmentList.get(0);
		DataWrapper allSerializedHeaders = dataGen.emptyWrapper();
		for (HasHeaderFragment iterFrame : hasHeaderFragmentList) {
		    allSerializedHeaders = dataGen.chainDataWrappers(allSerializedHeaders, iterFrame.getHeaderFragment());
		}
		
		List<Http2Header> headers = decoding.decode(state.getDecoder(), allSerializedHeaders, firstFrame.getStreamId());

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
    public MarshalState prepareToMarshal(int maxHeaderTableSize, long remoteMaxFrameSize) {
		Encoder encoder = new Encoder(maxHeaderTableSize);
		return new MarshalStateImpl(encoding, encoder, remoteMaxFrameSize);
	}

	@Override
	public DataWrapper marshal(MarshalState memento, Http2Msg msg) {
		if(memento == null || msg == null)
			throw new IllegalArgumentException("no parameters can be null");
		MarshalStateImpl state = (MarshalStateImpl) memento;
		if(msg instanceof Http2Headers) {
			Http2Headers h = (Http2Headers) msg;
			return createHeadersData(state, h);
		} else if(msg instanceof Http2Push) {
			Http2Push p = (Http2Push) msg;
			return createPushPromiseData(state, p);
		} else if(msg instanceof Http2Frame) {
			return parser.marshal((Http2Frame) msg);
		} else
			throw new IllegalStateException("bug, missing case for msg="+msg);
	}

	private DataWrapper createPushPromiseData(MarshalStateImpl state, Http2Push p) {
		long maxFrameSize = state.getMaxRemoteFrameSize();
		Encoder encoder = state.getEncoder();
    	List<Http2Frame> headerFrames = encoding.translateToFrames(maxFrameSize, encoder, p);
		return translate(headerFrames);
	}

	private DataWrapper createHeadersData(MarshalStateImpl state, Http2Headers headers) {
		long maxFrameSize = state.getMaxRemoteFrameSize();
		Encoder encoder = state.getEncoder();
    	List<Http2Frame> headerFrames = encoding.translateToFrames(maxFrameSize, encoder, headers);
		return translate(headerFrames);
	}

	private DataWrapper translate(List<Http2Frame> headerFrames) {
		DataWrapper allData = DataWrapperGeneratorFactory.EMPTY;
		for(Http2Frame f : headerFrames) {
			DataWrapper frameData = parser.marshal(f);
			allData = dataGen.chainDataWrappers(allData, frameData);
		}
		return allData;
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
