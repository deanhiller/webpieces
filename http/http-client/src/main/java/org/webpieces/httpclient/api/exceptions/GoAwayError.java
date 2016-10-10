package org.webpieces.httpclient.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.dto.Http2GoAway;
import org.webpieces.data.api.DataWrapper;

public class GoAwayError extends Http2Error {
    private int lastStreamId;
    private Http2ErrorCode errorCode;
    private DataWrapper debugData;

    public GoAwayError(int lastStreamId, Http2ErrorCode errorCode, DataWrapper debugData) {
        this.lastStreamId = lastStreamId;
        this.errorCode = errorCode;
        this.debugData = debugData;
    }

    @Override
    public Http2Frame toFrame() {
        Http2GoAway frame = new Http2GoAway();
        frame.setErrorCode(errorCode);
        frame.setLastStreamId(lastStreamId);
        frame.setDebugData(debugData);

        return frame;
    }
}
