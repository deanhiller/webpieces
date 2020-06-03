package com.webpieces.http2.api.dto.lowlevel;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2ErrorCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2FrameType;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;

public class RstStreamFrame extends AbstractHttp2Frame implements CancelReason {

    /* flags */

    /* payload */
    private long errorCode; //32 bits

    public RstStreamFrame() {
	}
    
    public RstStreamFrame(int streamId, Http2ErrorCode code) {
    	super(streamId);
    	setKnownErrorCode(code);
	}    
    
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
	public Http2MsgType getMessageType() {
		return Http2MsgType.RST_STREAM;
	}
	
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.RST_STREAM;
    }
	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (errorCode ^ (errorCode >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RstStreamFrame other = (RstStreamFrame) obj;
		if (errorCode != other.errorCode)
			return false;
		return true;
	}

	@Override
    public String toString() {
        return "RstStreamFrame{" +
        		super.toString() +
                ", errorCode=" + errorCode +
                "} ";
    }

	@Override
	public boolean isEndOfStream() {
		return true;
	}

}
