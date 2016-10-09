package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;

public class Http2Headers extends Http2Frame implements HasHeaderFragment, HasHeaderList, HasPriorityDetails {
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

    public boolean isEndHeaders() {
        return endHeaders;
    }

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

    public LinkedList<Header> getHeaderList() {
        return headerList;
    }

    public void setHeaderList(LinkedList<Header> headerList) {
        this.headerList = headerList;
    }

    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    public boolean isStreamDependencyIsExclusive() {
        return priorityDetails.streamDependencyIsExclusive;
    }

    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
        this.priorityDetails.streamDependencyIsExclusive = streamDependencyIsExclusive;
    }

    public int getStreamDependency() {
        return priorityDetails.streamDependency;
    }

    public void setStreamDependency(int streamDependency) {
        this.priorityDetails.streamDependency = streamDependency & 0x7FFFFFFF;
    }

    public short getWeight() {
        return priorityDetails.weight;
    }

    public void setWeight(short weight) {
        this.priorityDetails.weight = weight;
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

}
