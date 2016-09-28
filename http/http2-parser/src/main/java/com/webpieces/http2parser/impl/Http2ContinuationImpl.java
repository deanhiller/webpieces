package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2Continuation;
import com.webpieces.http2parser.api.Http2FrameType;
import org.webpieces.data.api.DataWrapper;

import java.util.ArrayList;
import java.util.List;
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
    private Http2HeaderBlock headerBlock;

    public DataWrapper getPayloadDataWrapper() {
        return headerBlock.getDataWrapper();
    }

    public void setPayloadFromDataWrapper(DataWrapper payload) {
        headerBlock = new Http2HeaderBlock(payload);
    }

    // ack copy/paste!
    public void setHeaders(Map<String, String> headers) {
        List<Http2HeaderBlock.Header> headerList = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerList.add(new Http2HeaderBlock.Header(entry.getKey(), entry.getValue()));
        }
        headerBlock = new Http2HeaderBlock(headerList);
    }

    public Map<String, String> getHeaders() {
        return headerBlock.toMap();
    }

}
