package org.webpieces.httpcommon.api;

import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2FrameType;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Http2FullPushPromise implements Http2Frame {

	private int promisedStreamId;
	private List<Http2Header> headerList;
	private int streamId;
	
	public Http2FullPushPromise(List<Http2Header> headers, int promisedStreamId2, int streamId) {
		this.headerList = headers;
		this.promisedStreamId = promisedStreamId2;
		this.streamId = streamId;
	}

	public boolean isEndHeaders() {
		return true;
	}

	public int getPromisedStreamId() {
		return promisedStreamId;
	}

	public List<Http2Header> getHeaderList() {
		return headerList;
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
		return Http2FrameType.FULL_PROMISE;
	}

}
