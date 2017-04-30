package com.webpieces.http2parser.api.dto;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class RstStreamFrame extends AbstractHttp2Frame implements PartialStream {

    /* flags */

    /* payload */
    private long errorCode; //32 bits

    public long getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(long errorCode) {
		this.errorCode = errorCode;
	}

	public Http2ErrorCode getKnownErrorCode() {
        return Http2ErrorCode.translate(errorCode);
    }

    public void setKnownErrorCode(Http2ErrorCode errorCode) {
        this.errorCode = errorCode.getCode();
    }

	@Override
	public boolean isEndOfStream() {
		return false;
	}
	
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.RST_STREAM;
    }
    @Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.RST_STREAM;
	}
	
    @Override
    public String toString() {
        return "RstStreamFrame{" +
        		super.toString() +
                ", errorCode=" + errorCode +
                "} ";
    }

}
