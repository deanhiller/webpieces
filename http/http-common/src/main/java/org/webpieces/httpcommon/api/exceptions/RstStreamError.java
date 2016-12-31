package org.webpieces.httpcommon.api.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

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
        RstStreamFrame frame = new RstStreamFrame();
        frame.setStreamId(streamId);
        frame.setErrorCode(errorCode);
        List<AbstractHttp2Frame> frames = new ArrayList<>();
        frames.add(frame);

        return frames;
    }
}
