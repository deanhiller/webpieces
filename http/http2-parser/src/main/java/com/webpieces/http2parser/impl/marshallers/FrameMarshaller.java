package com.webpieces.http2parser.impl.marshallers;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2parser.impl.Http2MementoImpl;

public interface FrameMarshaller {

	public DataWrapper marshal(Http2Frame frame);

	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData);
}
