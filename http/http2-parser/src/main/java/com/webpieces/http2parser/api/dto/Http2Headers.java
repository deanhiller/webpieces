package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.HeaderBlock;
import com.webpieces.http2parser.api.HeaderBlockFactory;
import com.webpieces.http2parser.api.Padding;
import com.webpieces.http2parser.api.PaddingFactory;

import java.util.Map;

public class Http2Headers extends Http2Frame {
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
    private HeaderBlock headerBlock = HeaderBlockFactory.createHeaderBlock();
    private Padding padding = PaddingFactory.createPadding();

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

    public void setHeaders(Map<String, String> headers) {
        headerBlock.setFromMap(headers);
    }

    public HeaderBlock getHeaderBlock() {
        return headerBlock;
    }

    public Map<String, String> getHeaders() {
        return headerBlock.getMap();
    }

}
