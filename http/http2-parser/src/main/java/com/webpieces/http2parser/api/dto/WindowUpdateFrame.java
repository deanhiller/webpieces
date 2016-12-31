package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class WindowUpdateFrame extends AbstractHttp2Frame {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.WINDOW_UPDATE;
    }

    /* flags */

    /* payload */
    //1bit reserved
    private int windowSizeIncrement; //31 bits

    public int getWindowSizeIncrement() {
        return windowSizeIncrement;
    }

    public void setWindowSizeIncrement(int windowSizeIncrement) {
        this.windowSizeIncrement = windowSizeIncrement & 0x7FFFFFFF;
    }

    @Override
    public String toString() {
        return "WindowUpdateFrame{" +
        		"streamId=" + super.toString() +
                ", windowSizeIncrement=" + windowSizeIncrement +
                "} ";
    }
}
