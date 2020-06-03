package com.webpieces.http2.api.dto.lowlevel.lib;

import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public abstract class AbstractHttp2Frame implements Http2Frame {
    protected static final DataWrapperGenerator DATA_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    public AbstractHttp2Frame() {
	}
    
    public AbstractHttp2Frame(int streamId2) {
    	this.streamId = streamId2;
	}

	//24 bits unsigned length
    public abstract Http2FrameType getFrameType(); //8bits

    //1bit reserved
    protected int streamId; //31 bits unsigned

    public void setStreamId(int streamId) {
    	this.streamId = streamId;
    }

    public int getStreamId() {
        return streamId;
    }

    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + streamId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractHttp2Frame other = (AbstractHttp2Frame) obj;
		if (streamId != other.streamId)
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "streamId=" + streamId;
    }
}
