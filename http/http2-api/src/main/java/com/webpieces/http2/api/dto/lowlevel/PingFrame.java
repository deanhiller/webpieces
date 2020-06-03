package com.webpieces.http2.api.dto.lowlevel;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2FrameType;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;

public class PingFrame extends AbstractHttp2Frame implements Http2Msg {

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
    public Http2FrameType getFrameType() {
        return Http2FrameType.PING;
    }
    @Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.PING;
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
