package com.webpieces.http2parser.api;

import com.webpieces.http2parser.api.dto.Http2Frame;
import org.webpieces.data.api.DataWrapper;

import java.util.Optional;

public interface FrameMarshaller {
    DataWrapper marshalPayload(Http2Frame frame);
    byte marshalFlags(Http2Frame frame);

    void unmarshalFlagsAndPayload(Http2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload);
}
