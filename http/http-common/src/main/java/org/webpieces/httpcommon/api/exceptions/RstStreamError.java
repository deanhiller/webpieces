package org.webpieces.httpcommon.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2RstStream;

import java.util.ArrayList;
import java.util.List;

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
    public List<Http2Frame> toFrames() {
        Http2RstStream frame = new Http2RstStream();
        frame.setStreamId(streamId);
        frame.setErrorCode(errorCode);
        List<Http2Frame> frames = new ArrayList<>();
        frames.add(frame);

        return frames;
    }
}
