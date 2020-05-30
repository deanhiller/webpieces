package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;

public class PushPromiseFrame extends AbstractHttp2Frame implements HasHeaderFragment {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.PUSH_PROMISE;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    // reserved - 1bit
    private int promisedStreamId = 0x0; //31bits
    private DataWrapper headerFragment;
    private DataWrapper padding = DataWrapperGeneratorFactory.EMPTY;

    @Override
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    public DataWrapper getPadding() {
		return padding;
	}

	public void setPadding(DataWrapper padding) {
		this.padding = padding;
	}

	public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
    	//TODO: This should be tested in marshaller and throw if promisedStreamId not equal to modified one
        this.promisedStreamId = promisedStreamId & 0x7FFFFFFF;
    }

    @Override
    public String toString() {
        return "PushPromiseFrame{" +
        		super.toString() +
                ", endHeaders=" + endHeaders +
                ", promisedStreamId=" + promisedStreamId +
                ", serializeHeaders=" + headerFragment.getReadableSize() +
                ", padding=" + padding +
                "} ";
    }
}
