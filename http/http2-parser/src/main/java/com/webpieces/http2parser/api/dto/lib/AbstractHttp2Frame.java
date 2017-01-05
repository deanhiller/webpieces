package com.webpieces.http2parser.api.dto.lib;

import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public abstract class AbstractHttp2Frame implements Http2Frame {
    protected final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    //24 bits unsigned length
    public abstract Http2FrameType getFrameType(); //8bits

    //1bit reserved
    private int streamId; //31 bits unsigned

    public void setStreamId(int streamId) {
    	this.streamId = streamId;
    }

    public int getStreamId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "streamId=" + streamId;
    }
}
