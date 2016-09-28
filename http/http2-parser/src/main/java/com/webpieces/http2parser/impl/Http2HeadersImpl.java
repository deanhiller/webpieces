package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2Headers;
import com.webpieces.http2parser.api.Http2Padded;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Http2HeadersImpl extends Http2FrameImpl implements Http2Headers {
    public Http2FrameType getFrameType() {
        return Http2FrameType.HEADERS;
    }

    /* flags */
    private boolean endStream = false; /* 0x1 */
    private boolean endHeaders = false; /* 0x4 */
    private boolean padded = false; /* 0x8 */
    private boolean priority = false; /* 0x20 */

    public byte getFlagsByte() {
        byte value = 0x0;
        if (endStream) value |= 0x1;
        if (endHeaders) value |= 0x4;
        if (padded) value |= 0x8;
        if (priority) value |= 0x20;
        return value;
    }

    public void setFlags(byte flags) {
        endStream = (flags & 0x1) == 0x1;
        endHeaders = (flags & 0x4) == 0x4;
        padded = (flags & 0x8) == 0x8;
        priority = (flags & 0x20) == 0x20;
    }

    public boolean isEndStream() {
        return endStream;
    }

    public void setEndStream() {
        this.endStream = true;
    }

    public boolean isEndHeaders() {
        return endHeaders;
    }

    public void setEndHeaders() {
        this.endHeaders = true;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority() {
        this.priority = true;
    }

    /* payload */
    private boolean streamDependencyIsExclusive = false; //1 bit
    private int streamDependency = 0x0; //31 bits
    private byte weight = 0x0; //8 bits
    private Http2HeaderBlockImpl headerBlock = new Http2HeaderBlockImpl();
    private byte[] padding = null;

    public boolean isStreamDependencyIsExclusive() {
        return streamDependencyIsExclusive;
    }

    public void setStreamDependencyIsExclusive() {
        this.streamDependencyIsExclusive = true;
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
        this.padding = padding;
        padded = true;
    }

    public void setHeaders(Map<String, String> headers) {
        headerBlock.setFromMap(headers);
    }

    public Map<String, String> getHeaders() {
        return headerBlock.getMap();
    }

    public DataWrapper getPayloadDataWrapper() {
        ByteBuffer prelude = ByteBuffer.allocate(5);
        prelude.putInt(streamDependency);
        if (streamDependencyIsExclusive) prelude.put(0, (byte) (prelude.get(0) | 0x80));
        prelude.put(weight);
        prelude.flip();

        DataWrapper unpadded = dataGen.chainDataWrappers(
                new ByteBufferDataWrapper(prelude),
                headerBlock.getDataWrapper());
        if (!padded) {
            return unpadded;
        } else {
            return Http2Padded.pad(padding, unpadded);
        }
    }

    public void setPayloadFromDataWrapper(DataWrapper payload) {
        List<? extends DataWrapper> split = dataGen.split(payload, 5);
        ByteBuffer prelude = ByteBuffer.wrap(split.get(0).createByteArray());
        int firstInt = prelude.getInt();
        streamDependencyIsExclusive = firstInt >>> 31 == 0x1;
        streamDependency = firstInt & 0x7FFFFFFF;
        weight = prelude.get();

        if (padded) {
            byte padLength = split.get(1).readByteAt(0);
            List<? extends DataWrapper> split1 = dataGen.split(split.get(1), 1);
            List<? extends DataWrapper> split2 = dataGen.split(split1.get(1), payload.getReadableSize() - padLength);
            headerBlock.setFromDataWrapper(split2.get(0));
            padding = split2.get(1).createByteArray();
        } else {
            headerBlock.setFromDataWrapper(split.get(1));
        }
    }
}
