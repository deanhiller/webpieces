package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;

public interface FrameMarshaller {
    DataWrapper getPayloadDataWrapper(Http2Frame frame);
    byte getFlagsByte(Http2Frame frame);
}
