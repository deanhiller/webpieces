package org.webpieces.httpcommon.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2Frame;

import java.util.List;

public abstract class Http2Error extends RuntimeException {
    public abstract List<Http2Frame> toFrames();
}

