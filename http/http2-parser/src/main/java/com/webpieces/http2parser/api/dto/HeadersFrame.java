package com.webpieces.http2parser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

public class HeadersFrame extends AbstractHttp2Frame implements HasHeaderFragment {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.HEADERS;
    }

    /* flags */
    private boolean endOfStream = false; /* 0x1 */
    private boolean endHeaders = false; /* 0x4 */
    //private boolean padded = false; /* 0x8 */
    //if PriorityDetails is null, this is false
    //private boolean priority = false; /* 0x20 */
    /* payload */
    private PriorityDetails priorityDetails; /* optional */
    private DataWrapper headerFragment;
    private DataWrapper padding = DataWrapperGeneratorFactory.EMPTY;
    
    public boolean isEndOfStream() {
        return endOfStream;
    }

    public void setEndOfStream(boolean endStream) {
        this.endOfStream = endStream;
    }

    @Override
    public boolean isEndHeaders() {
        return endHeaders;
    }

    @Override
    public void setEndHeaders(boolean endHeaders) {
        this.endHeaders = endHeaders;
    }

    public boolean isPriority() {
        return priorityDetails != null;
    }

    @Override
    public DataWrapper getHeaderFragment() {
        return headerFragment;
    }

    @Override
    public void setHeaderFragment(DataWrapper serializedHeaders) {
        this.headerFragment = serializedHeaders;
    }

    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

    public DataWrapper getPadding() {
		return padding;
	}

	public void setPadding(DataWrapper padding) {
		this.padding = padding;
	}

	@Override
    public String toString() {
        return "HeadersFrame{" +
        		super.toString() +
                ", endStream=" + endOfStream +
                ", endHeaders=" + endHeaders +
                ", priorityDetails=" + priorityDetails +
                ", headerFragment=" + headerFragment.getReadableSize() +
                ", padding=" + padding +
                "} ";
    }
}
