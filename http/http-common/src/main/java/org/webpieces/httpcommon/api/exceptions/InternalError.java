package org.webpieces.httpcommon.api.exceptions;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;

public class InternalError extends GoAwayError {
    public InternalError(int lastStreamId, DataWrapper debugData) {
        super(lastStreamId, Http2ErrorCode.INTERNAL_ERROR, debugData);
    }
}
