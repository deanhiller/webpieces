package com.webpieces.http2parser.api;

import java.util.Optional;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.Http2Frame;

public interface FrameMarshaller {
    DataWrapper marshalPayload(Http2Frame frame);
    byte marshalFlags(Http2Frame frame);

    void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload);
}
