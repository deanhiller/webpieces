package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class Http2Continuation extends Http2Frame implements HasHeaderFragment {

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.CONTINUATION;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    private DataWrapper headerFragment;

    @Override
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    @Override
    public String toString() {
        return "Http2Continuation{" +
                "endHeaders=" + endHeaders +
                ", serializeHeaders=" + headerFragment +
                "} " + super.toString();
    }
}
