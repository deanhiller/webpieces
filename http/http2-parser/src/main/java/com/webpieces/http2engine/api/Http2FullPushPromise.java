package com.webpieces.http2engine.api;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Http2FullPushPromise implements Http2Payload {

	//NOTE: This streamId is the new one unlike the spec while Http2PushPromise streamId matches the spec
	private int streamId;
    private List<Http2Header> headerList = new ArrayList<>(); 
    private int originalStreamId; //The new stream being created which becomes the streamId of all the following responses
    
	public int getStreamId() {
		return streamId;
	}
	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	public List<Http2Header> getHeaderList() {
		return headerList;
	}
	public void setHeaderList(List<Http2Header> headerList) {
		this.headerList = headerList;
	}

	@Override
	public boolean isEndStream() {
		return false;
	}
	public int getOriginalStreamId() {
		return originalStreamId;
	}
	public void setOriginalStreamId(int originalStreamId) {
		this.originalStreamId = originalStreamId;
	}
    
}
