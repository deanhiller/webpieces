package org.webpieces.httpcommon.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2RstStream;

public class RstStreamError extends Http2Error {
    private Http2ErrorCode errorCode;
    private int streamId;

    public int getStreamId() {
        return streamId;
    }

    public RstStreamError(Http2ErrorCode errorCode, int streamId) {
        this.errorCode = errorCode;
        this.streamId = streamId;
    }

    @Override
    public Http2Frame toFrame() {
        Http2RstStream frame = new Http2RstStream();
        frame.setStreamId(streamId);
        frame.setErrorCode(errorCode);
        return frame;
    }
}