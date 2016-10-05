package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;

public class Http2PushPromise extends Http2Frame implements HasHeaderFragment, HasHeaderList {

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
    private DataWrapper headerFragment;
    private Padding padding = PaddingFactory.createPadding();
    private LinkedList<Header> headerList; // only created by the parser when deserializing a bunch of header frames
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    public LinkedList<Header> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(LinkedList<Header> headerList) {
        this.headerList = headerList;
    }

    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
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

    @Override
    public String toString() {
        return "Http2PushPromise{" +
                "endHeaders=" + endHeaders +
                ", promisedStreamId=" + promisedStreamId +
                ", serializeHeaders=" + headerFragment +
                ", padding=" + padding +
                "} " + super.toString();
    }
}
