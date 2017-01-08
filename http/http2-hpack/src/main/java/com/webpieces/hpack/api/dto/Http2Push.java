package com.webpieces.hpack.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http2Push implements PartialStream {

	private int streamId;
	private int promisedStreamId;
	protected List<Http2Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Http2HeaderStruct headersStruct = new Http2HeaderStruct();

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
	
	public int getPromisedStreamId() {
		return promisedStreamId;
	}
	public void setPromisedStreamId(int promisedStreamId) {
		this.promisedStreamId = promisedStreamId;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + promisedStreamId;
		result = prime * result + streamId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Http2Push other = (Http2Push) obj;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (promisedStreamId != other.promisedStreamId)
			return false;
		if (streamId != other.streamId)
			return false;
		return true;
	}
	
	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.PUSH_PROMISE;
	}
	
	@Override
	public String toString() {
		return "Http2Push [streamId=" + streamId + ", promisedStreamId=" + promisedStreamId +  ", headers=" + headers + "]";
	}
	
}
