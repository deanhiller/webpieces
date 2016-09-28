package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2WindowUpdate;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;

public class Http2WindowUpdateImpl extends Http2FrameImpl implements Http2WindowUpdate {
    public Http2FrameType getFrameType() {
        return Http2FrameType.WINDOW_UPDATE;
    }

    /* flags */
    public byte getFlagsByte() {
        return 0x0;
    }

    public void setFlags(byte flags) {
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

    public DataWrapper getPayloadDataWrapper() {
        ByteBuffer payload = ByteBuffer.allocate(4).putInt(windowSizeIncrement);
        payload.flip();

        return new ByteBufferDataWrapper(payload);
    }

    public void setPayloadFromDataWrapper(DataWrapper payload) {
        ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
        windowSizeIncrement = payloadByteBuffer.getInt() & 0x7FFFFFFF;
    }
}
