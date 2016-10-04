package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;

public class Http2Continuation extends Http2Frame implements HasHeaders {

    public Http2FrameType getFrameType() {
        return Http2FrameType.CONTINUATION;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */

    public boolean isEndHeaders() {
        return endHeaders;
    }

    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    private LinkedList<Header> headers;
    private DataWrapper serializedHeaders;

    public void setHeaders(LinkedList<Header> headers) {
        this.headers = headers;
    }

    public LinkedList<Header> getHeaders() {
        return headers;
    }

    @Override
    public DataWrapper getSerializedHeaders() {
        return serializedHeaders;
    }

    public void setSerializedHeaders(DataWrapper serializedHeaders) {
        this.serializedHeaders = serializedHeaders;
    }

    @Override
    public String toString() {
        return "Http2Continuation{" +
                "endHeaders=" + endHeaders +
                ", headers=" + headers +
                ", serializedHeaders=" + serializedHeaders +
                "} " + super.toString();
    }
}
