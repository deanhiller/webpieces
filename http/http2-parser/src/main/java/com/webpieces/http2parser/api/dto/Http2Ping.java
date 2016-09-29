package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;

import java.nio.ByteBuffer;

public class Http2Ping extends Http2Frame {
    public Http2FrameType getFrameType() {
        return Http2FrameType.PING;
    }

    /* flags */
    private boolean isPingResponse = false; /* 0x1 */

    public void unmarshalFlags(byte flags) {
        isPingResponse = (flags & 0x1) == 0x1;
    }

    public boolean isPingResponse() {
        return isPingResponse;
    }

    public void setPingResponse() {
        isPingResponse = true;
    }

    /* payload */
    private long opaqueData = 0x0;

    public long getOpaqueData() {
        return opaqueData;
    }

    public void setOpaqueData(long opaqueData) {
        this.opaqueData = opaqueData;
    }


    public void unmarshalPayload(DataWrapper payload) {
        ByteBuffer payloadByteBuffer = ByteBuffer.wrap(payload.createByteArray());
        opaqueData = payloadByteBuffer.getLong();
    }
}
