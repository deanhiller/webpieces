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
    	//TODO: remove this.  all precondition code is in marshallers and unmarshallers now
    	//putting it in the beans never seems to work out well
    	
        // Clear the MSB because streamId can only be 31 bits
        this.streamId = streamId & 0x7FFFFFFF;
        if(this.streamId != streamId) 
        	throw new RuntimeException("your stream id is too large");
    }

    public int getStreamId() {
        return streamId;
    }

    @Override
    public String toString() {
        return "streamId=" + streamId;
    }
}
