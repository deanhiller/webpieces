package com.webpieces.http2.api.dto.lowlevel;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.HasHeaderFragment;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2FrameType;

public class ContinuationFrame extends AbstractHttp2Frame implements HasHeaderFragment {

    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.CONTINUATION;
    }

    /* flags */
    private boolean endHeaders = false; /* 0x4 */

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    /* payload */
    private DataWrapper headerFragment;

    @Override
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    @Override
    public String toString() {
        return "ContinuationFrame{" +
        		super.toString() +
                "endHeaders=" + endHeaders +
                ", serializeHeaders=" + headerFragment +
                "} ";
    }
}
