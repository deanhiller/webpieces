package com.webpieces.http2engine.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Http2Push implements PartialStream {

	//the new stream id being created
	private int streamId; 
	protected List<Http2Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Http2HeaderStruct headersStruct = new Http2HeaderStruct();
	//the stream id that caused this push promise to start
    private int causalStreamId; 

	public Http2Push() {}
	public Http2Push(List<Http2Header> headerList) {
		for(Http2Header header : headerList) {
			addHeader(header);
		}
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}
	
	/**
	 * Order of HTTP Headers matters for Headers with the same key
	 */
	public List<Http2Header> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	public void addHeader(Http2Header header) {
		headers.add(header);
		headersStruct.addHeader(header);
	}
	
	/** 
	 * 
	 * @return
	 */
	public Http2HeaderStruct getHeaderLookupStruct() {
		return headersStruct;
	}
	@Override
	public boolean isEndOfStream() {
		return false;
	}
	@Override
	public String toString() {
		return "Http2Push [streamId=" + streamId + ", causalStreamId=" + getCausalStreamId() +  ", headers=" + headers + "]";
	}
	public int getCausalStreamId() {
		return causalStreamId;
	}
	public void setCausalStreamId(int causalStreamId) {
		this.causalStreamId = causalStreamId;
	}
	
}
