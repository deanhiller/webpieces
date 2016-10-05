package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;
import org.webpieces.data.api.DataWrapper;

import java.util.LinkedList;

public class Http2Headers extends Http2Frame implements HasHeaderFragment, HasHeaderList {
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

    /* payload */
    private boolean streamDependencyIsExclusive = false; //1 bit
    private int streamDependency = 0x0; //31 bits
    private byte weight = 0x0; //8 bits
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
        return streamDependencyIsExclusive;
    }

    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
        this.streamDependencyIsExclusive = streamDependencyIsExclusive;
    }

    public int getStreamDependency() {
        return streamDependency;
    }

    public void setStreamDependency(int streamDependency) {
        this.streamDependency = streamDependency & 0x7FFFFFFF;
    }

    public byte getWeight() {
        return weight;
    }

    public void setWeight(byte weight) {
        this.weight = weight;
    }

    public void setPadding(byte[] padding) {
        this.padding.setPadding(padding);
    }

    public Padding getPadding() {
        return this.padding;
    }

    @Override
    public String toString() {
        return "Http2Headers{" +
                "endStream=" + endStream +
                ", endHeaders=" + endHeaders +
                ", priority=" + priority +
                ", streamDependencyIsExclusive=" + streamDependencyIsExclusive +
                ", streamDependency=" + streamDependency +
                ", weight=" + weight +
                ", serializeHeaders=" + headerFragment +
                ", padding=" + padding +
                "} " + super.toString();
    }
}
