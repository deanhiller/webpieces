package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class PingFrame extends AbstractHttp2Frame {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.PING;
    }

    /* flags */
    private boolean isPingResponse = false; /* 0x1 */
    /* payload */
    private long opaqueData = 0x0;

    public boolean isPingResponse() {
        return isPingResponse;
    }

    public void setIsPingResponse(boolean isPingResponse) {
        this.isPingResponse = isPingResponse;
    }

    public long getOpaqueData() {
        return opaqueData;
    }

    public void setOpaqueData(long opaqueData) {
        this.opaqueData = opaqueData;
    }

    @Override
    public String toString() {
        return "PingFrame{" +
        		super.toString() +
                "isPingResponse=" + isPingResponse +
                ", opaqueData=" + opaqueData +
                "} ";
    }
}
