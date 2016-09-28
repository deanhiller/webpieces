package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2Continuation;
import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2HeaderBlock;
import org.webpieces.data.api.DataWrapper;

import java.util.Map;

public class Http2ContinuationImpl extends Http2FrameImpl implements Http2Continuation {

    public Http2FrameType getFrameType() {
        return Http2FrameType.CONTINUATION;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */

    public byte getFlagsByte() {
        byte value = 0x0;
        if (endHeaders) value |= 0x4;
        return value;
    }

    public void setFlags(byte flags) {
        endHeaders = (flags & 0x4) == 0x4;
    }

    public boolean isEndHeaders() {
        return endHeaders;
    }

    public void setEndHeaders() {
        this.endHeaders = true;
    }

    /* payload */
    private Http2HeaderBlock headerBlock = new Http2HeaderBlockImpl();

    public DataWrapper getPayloadDataWrapper() {
        return headerBlock.getDataWrapper();
    }

    public void setPayloadFromDataWrapper(DataWrapper payload) {
        headerBlock.setFromDataWrapper(payload);
    }

    // ack copy/paste!
    public void setHeaders(Map<String, String> headers) {
        headerBlock.setFromMap(headers);
    }

    public Map<String, String> getHeaders() {
        return headerBlock.getMap();
    }

}
