package com.webpieces.http2parser.api;

import java.util.Optional;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;

public interface FrameMarshaller {
    DataWrapper marshalPayload(AbstractHttp2Frame frame);
    byte marshalFlags(AbstractHttp2Frame frame);

    void unmarshalFlagsAndPayload(AbstractHttp2Frame frame, byte flagsByte, Optional<DataWrapper> maybePayload);
}
