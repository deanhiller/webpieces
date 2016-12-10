package com.webpieces.http2parser.api.dto;

import java.util.LinkedList;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;

public class Http2Headers extends Http2Frame implements HasHeaderFragment, HasHeaderList, HasPriorityDetails {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.HEADERS;
    }

    /* flags */
    private boolean endStream = false; /* 0x1 */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */
    private boolean priority = false; /* 0x20 */

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
        return priority;
    }
    public void setPriority(boolean priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "Http2Headers{" +
                "endStream=" + endStream +
                ", endHeaders=" + endHeaders +
                ", priority=" + priority +
                ", priorityDetails=" + priorityDetails +
                ", headerFragment=" + headerFragment +
                ", headerList=" + headerList +
                ", padding=" + padding +
                "} " + super.toString();
    }

    /* payload */
    private PriorityDetails priorityDetails = new PriorityDetails(); /* optional */
    private DataWrapper headerFragment;
    private LinkedList<Header> headerList; // only created by the parser when deserializing a bunch of header frames
    private Padding padding = PaddingFactory.createPadding();

    @Override
    public LinkedList<Header> getHeaderList() {
        return headerList;
    }

    @Override
    public void setHeaderList(LinkedList<Header> headerList) {
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

    @Override
    public boolean isStreamDependencyIsExclusive() {
        return priorityDetails.streamDependencyIsExclusive;
    }

    @Override
    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
        this.priorityDetails.streamDependencyIsExclusive = streamDependencyIsExclusive;
    }

    @Override
    public int getStreamDependency() {
        return priorityDetails.streamDependency;
    }

    @Override
    public void setStreamDependency(int streamDependency) {
        this.priorityDetails.streamDependency = streamDependency & 0x7FFFFFFF;
    }

    @Override
    public short getWeight() {
        return priorityDetails.weight;
    }

    @Override
    public void setWeight(short weight) {
        this.priorityDetails.weight = weight;
    }

    @Override
    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    @Override
    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

    public Padding getPadding() {
        return this.padding;
    }

}
