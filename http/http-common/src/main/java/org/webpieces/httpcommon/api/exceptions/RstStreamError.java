package org.webpieces.httpcommon.api.exceptions;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

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
    
    public RstStreamError(Http2ErrorCode errorCode, int streamId, Throwable e) {
    	super(e);
        this.errorCode = errorCode;
        this.streamId = streamId;
    }

    @Override
    public List<Http2Msg> toFrames() {
        RstStreamFrame frame = new RstStreamFrame();
        frame.setStreamId(streamId);
        frame.setErrorCode(errorCode);
        List<Http2Msg> frames = new ArrayList<>();
        frames.add(frame);

        return frames;
    }
}
