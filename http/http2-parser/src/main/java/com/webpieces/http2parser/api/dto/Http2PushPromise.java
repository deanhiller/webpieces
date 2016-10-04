package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;
import java.util.List;

public class Http2PushPromise extends Http2Frame implements HasHeaders {

    public Http2FrameType getFrameType() {
        return Http2FrameType.PUSH_PROMISE;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */

    public boolean isEndHeaders() {
        return endHeaders;
    }

    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    // reserved - 1bit
    private int promisedStreamId = 0x0; //31bits
    private LinkedList<Header> headers;
    private DataWrapper serializedHeaders;
    private Padding padding = PaddingFactory.createPadding();

    public DataWrapper getSerializedHeaders() {
        return serializedHeaders;
    }

    public void setSerializedHeaders(DataWrapper serializedHeaders) {
        this.serializedHeaders = serializedHeaders;
    }

    public void setPadding(byte[] padding) {
        this.padding.setPadding(padding);
    }

    public Padding getPadding() {
        return padding;
    }

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId & 0x7FFFFFFF;
    }

    public LinkedList<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedList<Header> headers) {
        this.headers = headers;
    }
}
