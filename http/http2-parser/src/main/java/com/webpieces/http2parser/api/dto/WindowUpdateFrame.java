package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

public class WindowUpdateFrame extends AbstractHttp2Frame implements Http2Msg {

    /* flags */

    /* payload */
    //1bit reserved
    private int windowSizeIncrement; //31 bits

    public WindowUpdateFrame() {
	}
    public WindowUpdateFrame(int streamId, int size) {
    	super(streamId);
    	this.windowSizeIncrement = size;
	}    
    
    public int getWindowSizeIncrement() {
        return windowSizeIncrement;
    }

    public void setWindowSizeIncrement(int windowSizeIncrement) {
        this.windowSizeIncrement = windowSizeIncrement & 0x7FFFFFFF;
    }

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.WINDOW_UPDATE;
    }
    @Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.WINDOW_UPDATE;
	}
    
    @Override
    public String toString() {
        return "WindowUpdateFrame{" +
        		super.toString() +
                ", windowSizeIncrement=" + windowSizeIncrement +
                "} ";
    }

}
