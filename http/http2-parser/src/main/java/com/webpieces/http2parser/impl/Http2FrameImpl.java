package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2Frame;
import com.webpieces.http2parser.api.Http2FrameType;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.impl.ByteBufferDataWrapper;
import org.webpieces.data.impl.ChainedDataWrapper;

import java.nio.ByteBuffer;

public abstract class Http2FrameImpl implements Http2Frame {
    static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    //24 bits unsigned length
    public abstract Http2FrameType getFrameType(); //8bits

    //1bit reserved
    private int streamId; //31 bits unsigned

    public void setStreamId(int streamId) {
        // Clear the MSB because streamId can only be 31 bits
        this.streamId = streamId & 0x7FFFFFFF;
    }

    public int getStreamId() {
        return streamId;
    }

    // Look in ObjectTranslator for classtoMarshaller examples, use bufferPools
    public DataWrapper getDataWrapper() {
        ByteBuffer header = ByteBuffer.allocate(9);
        DataWrapper payload = getPayloadDataWrapper();

        int length = payload.getReadableSize();
        header.put((byte) (length >>> 16));
        header.putShort((short) length);

        header.put(getFrameTypeByte());
        header.put(getFlagsByte());
        // 1 bit reserved, streamId MSB is always 0, see setStreamId()
        header.putInt(streamId);
        header.flip();

        return dataGen.chainDataWrappers(dataGen.wrapByteBuffer(header), payload);
    }

    // The payload doesn't have any extra data past the end of the frame by now
    public abstract void setPayloadFromDataWrapper(DataWrapper payload);

    public byte[] getBytes() {
        return getDataWrapper().createByteArray();
    }

    private byte getFrameTypeByte() {
        return getFrameType().getId();
    }

    public abstract byte getFlagsByte();

    public abstract void setFlags(byte flag);

    abstract public DataWrapper getPayloadDataWrapper();
}
