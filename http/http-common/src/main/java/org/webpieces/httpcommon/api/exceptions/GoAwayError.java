package org.webpieces.httpcommon.api.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.RstStreamFrame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class GoAwayError extends Http2Error {
    private int lastStreamId;
    private int streamId = 0x0;
    private Http2ErrorCode errorCode;
    private DataWrapper debugData;

    public GoAwayError(int lastStreamId, Http2ErrorCode errorCode, DataWrapper debugData) {
        this.lastStreamId = lastStreamId;
        this.errorCode = errorCode;
        this.debugData = debugData;
    }

    public GoAwayError(int lastStreamId, Integer streamId, Http2ErrorCode errorCode, DataWrapper debugData) {
        this.lastStreamId = lastStreamId;
        this.streamId = streamId;
        this.errorCode = errorCode;
        this.debugData = debugData;
    }

    @Override
    public List<Http2Frame> toFrames() {
        List<Http2Frame> frames = new ArrayList<>();

        GoAwayFrame http2GoAway = new GoAwayFrame();
        http2GoAway.setErrorCode(errorCode);
        http2GoAway.setLastStreamId(lastStreamId);
        http2GoAway.setDebugData(debugData);
        frames.add(http2GoAway);
        if(streamId != 0x0) {
            RstStreamFrame http2RstStream = new RstStreamFrame();
            http2RstStream.setErrorCode(errorCode);
            http2RstStream.setStreamId(streamId);
            frames.add(http2RstStream);
        }

        return frames;
    }
}
