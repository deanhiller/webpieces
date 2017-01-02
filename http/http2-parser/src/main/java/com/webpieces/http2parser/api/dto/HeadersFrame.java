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
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

public class HeadersFrame extends AbstractHttp2Frame implements HasHeaderFragment, HasHeaderList {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.HEADERS;
    }

    /* flags */
    private boolean endStream = false; /* 0x1 */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */
    //if PriorityDetails is null, this is false
    //private boolean priority = false; /* 0x20 */
    /* payload */
    private PriorityDetails priorityDetails; /* optional */
    private DataWrapper headerFragment;
    private List<Http2Header> headerList; // only created by the parser when deserializing a bunch of header frames
    private Padding padding = PaddingFactory.createPadding();
    
    public boolean isEndStream() {
        return endStream;
    }

    public void setEndStream(boolean endStream) {
        this.endStream = endStream;
    }

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    public boolean isPriority() {
        return priorityDetails != null;
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
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

    public Padding getPadding() {
        return this.padding;
    }


    @Override
    public String toString() {
        return "HeadersFrame{" +
        		"streamId=" + super.toString() +
                ", endStream=" + endStream +
                ", endHeaders=" + endHeaders +
                ", priorityDetails=" + priorityDetails +
                ", headerFragment=" + headerFragment.getReadableSize() +
                ", padding=" + padding +
                "} ";
    }
}
