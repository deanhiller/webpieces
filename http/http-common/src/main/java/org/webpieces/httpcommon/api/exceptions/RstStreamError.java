package org.webpieces.httpcommon.api.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.AbstractHttp2Frame;
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
    public List<AbstractHttp2Frame> toFrames() {
        Http2RstStream frame = new Http2RstStream();
        frame.setStreamId(streamId);
        frame.setErrorCode(errorCode);
        List<AbstractHttp2Frame> frames = new ArrayList<>();
        frames.add(frame);

        return frames;
    }
}
