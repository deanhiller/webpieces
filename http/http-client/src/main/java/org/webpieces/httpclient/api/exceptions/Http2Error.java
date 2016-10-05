package org.webpieces.httpclient.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2Frame;

public abstract class Http2Error extends RuntimeException {
    public abstract Http2Frame toFrame();
}

