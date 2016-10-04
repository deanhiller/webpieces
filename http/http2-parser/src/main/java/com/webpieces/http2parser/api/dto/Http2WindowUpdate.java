package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.nio.ByteBuffer;

public class Http2WindowUpdate extends Http2Frame {
    public Http2FrameType getFrameType() {
        return Http2FrameType.WINDOW_UPDATE;
    }

    /* flags */


    public void unmarshalFlags(byte flags) {
    }

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
        return "Http2WindowUpdate{" +
                "windowSizeIncrement=" + windowSizeIncrement +
                "} " + super.toString();
    }
}
