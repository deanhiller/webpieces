package org.webpieces.httpclient.api.exceptions;

import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import org.webpieces.data.api.DataWrapper;

public class InternalError extends GoAwayError {
    public InternalError(int lastStreamId, DataWrapper debugData) {
        super(lastStreamId, Http2ErrorCode.INTERNAL_ERROR, debugData);
    }
}
