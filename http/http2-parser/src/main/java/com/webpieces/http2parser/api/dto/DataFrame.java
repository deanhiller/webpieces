package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class DataFrame extends AbstractHttp2Frame {

    /* flags */
    private boolean endStream = false; /* 0x1 */
    //private boolean padded = false;    /* 0x8 */
    /* payload */
    private DataWrapper data = dataGen.emptyWrapper();
    private DataWrapper padding = dataGen.emptyWrapper();
    
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.DATA;
    }

    public boolean isEndStream() {
        return endStream;
    }

    public void setEndStream(boolean endStream) {
        this.endStream = endStream;
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
    public String toString() {
        return "DataFrame{" +
        		super.toString() +
                ", endStream=" + endStream +
                ", data.len=" + data.getReadableSize() +
                ", padding=" + padding.getReadableSize() +
                "} ";
    }
}
