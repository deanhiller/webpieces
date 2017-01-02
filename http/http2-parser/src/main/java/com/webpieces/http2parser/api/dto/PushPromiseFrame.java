package com.webpieces.http2parser.api.dto;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.HasHeaderList;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class PushPromiseFrame extends AbstractHttp2Frame implements HasHeaderFragment, HasHeaderList {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.PUSH_PROMISE;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    // reserved - 1bit
    private int promisedStreamId = 0x0; //31bits
    private DataWrapper headerFragment;
    private Padding padding = PaddingFactory.createPadding();
    private List<Http2Header> headerList; // only created by the parser when deserializing a bunch of header frames

    @Override
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public List<Http2Header> getHeaderList() {
        return headerList;
    }

    @Override
    public void setHeaderList(List<Http2Header> headerList) {
        this.headerList = headerList;
    }

    @Override
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
        return "PushPromiseFrame{" +
                "endHeaders=" + endHeaders +
                ", promisedStreamId=" + promisedStreamId +
                ", serializeHeaders=" + headerFragment.getReadableSize() +
                ", padding=" + padding +
                "} " + super.toString();
    }
}
