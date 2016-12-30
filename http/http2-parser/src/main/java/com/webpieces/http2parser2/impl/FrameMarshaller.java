package com.webpieces.http2parser2.impl;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.Http2Frame;

public interface FrameMarshaller {

	public DataWrapper marshal(Http2Frame frame);

	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData);
}
