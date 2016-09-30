package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public abstract class Http2Frame {
    final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

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
}
