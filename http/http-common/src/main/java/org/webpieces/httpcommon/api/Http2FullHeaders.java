package org.webpieces.httpcommon.api;

import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;

public class Http2FullHeaders implements Http2Frame {

	private List<Http2Header> headers;
	private boolean endStream;
	private boolean priority;
	private PriorityDetails priorityDetails;
	private int streamId;

	public Http2FullHeaders(int streamId, List<Http2Header> headers2, PriorityDetails priorityDetails2, boolean endStream) {
		this.streamId = streamId;
		this.headers = headers2;
		this.priorityDetails = priorityDetails2;
		this.endStream = endStream;
	}

	public List<Http2Header> getHeaderList() {
		return headers;
	}

	public boolean isEndStream() {
		return endStream;
	}

	public boolean isPriority() {
		return priority;
	}

	public PriorityDetails getPriorityDetails() {
		return priorityDetails;
	}

	public boolean isEndHeaders() {
		return true;
	}

	@Override
	public int getStreamId() {
		return streamId;
	}

	@Override
	public void setStreamId(int id) {
		this.streamId = id;
	}

	@Override
	public Http2FrameType getFrameType() {
		return Http2FrameType.FULL_HEADERS;
	}

}
