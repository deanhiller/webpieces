package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class DataFrame extends AbstractHttp2Frame implements StreamMsg {

    /* flags */
	//defaulted to true as it is easier 
	//  1. to notice a stream ending immediately instead if it was false
	//  2. having something hang and wondering where it is(though that could be hard as well)
    private boolean endOfStream = true; /* 0x1 */
    //private boolean padded = false;    /* 0x8 */
    /* payload */
    private DataWrapper data = DATA_GEN.emptyWrapper();
    private DataWrapper padding = DATA_GEN.emptyWrapper();

    public DataFrame() {}
    
    public DataFrame(int streamId, boolean endOfStream) {
    	this.streamId = streamId;
    	this.endOfStream = endOfStream;
	}

	public boolean isEndOfStream() {
        return endOfStream;
    }

    public void setEndOfStream(boolean endStream) {
        this.endOfStream = endStream;
    }

    public DataWrapper getData() {
        return data;
    }

    public void setData(DataWrapper data) {
        this.data = data;
    }

    public DataWrapper getPadding() {
		return padding;
	}

	public void setPadding(DataWrapper padding) {
		this.padding = padding;
	}

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.DATA;
    }
    @Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.DATA;
	}
	
	@Override
    public String toString() {
        return "DataFrame{" +
        		super.toString() +
                ", endStream=" + endOfStream +
                ", data.len=" + data.getReadableSize() +
                ", padding=" + padding.getReadableSize() +
                "}";
    }

	public long getTransmitFrameLength() {
		long len = data.getReadableSize();
		long padLen = padding.getReadableSize();
		if(padLen > 0)
			padLen += 1;
		return len + padLen;
	}

}
