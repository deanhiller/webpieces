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
import com.webpieces.http2parser.api.dto.Http2Ping;
import com.webpieces.http2parser.api.dto.Http2Priority;

public class Http2Parser2Impl implements Http2Parser2 {

    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
    private final Map<Class<? extends Http2Frame>, FrameMarshaller> dtoToMarshaller = new HashMap<>();

	public Http2Parser2Impl(BufferPool bufferPool) {
        dtoToMarshaller.put(Http2Ping.class, new PingMarshaller(bufferPool, dataGen));
        dtoToMarshaller.put(Http2Priority.class, new PriorityMarshaller(bufferPool, dataGen));
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
		FrameMarshaller marshaller = dtoToMarshaller.get(frame.getClass());
		if(marshaller == null)
			throw new IllegalArgumentException("unknown frame bean="+frame);
		return marshaller.marshal(frame);
	}

}
