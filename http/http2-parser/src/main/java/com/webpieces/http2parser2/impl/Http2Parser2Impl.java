package com.webpieces.http2parser2.impl;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2FrameType;

public class Http2Parser2Impl implements Http2Parser2 {

    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private final Map<Http2FrameType, FrameMarshaller> dtoToMarshaller = new HashMap<>();

	public Http2Parser2Impl(BufferPool bufferPool) {
        dtoToMarshaller.put(Http2FrameType.CONTINUATION, new ContinuationMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.DATA, new DataMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.GOAWAY, new GoAwayMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.HEADERS, new HeadersMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.PING, new PingMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.PRIORITY, new PriorityMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.PUSH_PROMISE, new PushPromiseMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2FrameType.RST_STREAM, new RstStreamMarshaller(bufferPool, dataGen));
	}

	@Override
	public Http2Memento prepareToParse(Decoder decoder) {
		return new Http2MementoImpl(decoder, new Http2SettingsMap());
	}

	@Override
	public Http2Memento parse(Http2Memento memento, DataWrapper newData) {
		return null;
	}

	@Override
	public DataWrapper marshal(Http2Frame frame) {
		FrameMarshaller marshaller = dtoToMarshaller.get(frame.getFrameType());
		if(marshaller == null)
			throw new IllegalArgumentException("unknown frame bean="+frame);
		return marshaller.marshal(frame);
	}

}
