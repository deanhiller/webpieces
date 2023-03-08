package com.webpieces.http2.api.dto.highlevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2.api.dto.lowlevel.lib.PriorityDetails;

public abstract class Http2Headers implements Http2Msg {

	private int streamId;
	
	
	/** 
	 * VERY VERY VERY IMPORTANT: We default to true!  If we default to false, developers forget to set this to true, and
	 * it hangs, and the developer has tons of fun spending hours looking through millions of lines of code with no idea 
	 * what the issue is.  IF WE SET THIS TO true however, a stack trace will ensue because typically they will accidentally
	 * send two payloads with this set to true causing a nice exception saying, we see you are sending response X with 
	 * endOfStream=true BUT you already sent response Y with endOfStream=true making this situation much easier to debug
	 *
	 * Being practical of theoritical for the #win
	 */
	private boolean endOfStream = true;
	
    private PriorityDetails priorityDetails; /* optional */
	protected List<Http2Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Http2HeaderStruct headersStruct = new Http2HeaderStruct();

	public Http2Headers() {}
	public Http2Headers(List<Http2Header> headerList) {
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
	
	public PriorityDetails getPriorityDetails() {
		return priorityDetails;
	}
	public void setPriorityDetails(PriorityDetails priorityDetails) {
		this.priorityDetails = priorityDetails;
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
	
	public String getSingleHeaderValue(String name) {
		if(name == null)
			throw new IllegalArgumentException("argument name cannot be null");
		name = name.toLowerCase();
		Http2Header header = getHeaderLookupStruct().getHeader(name);
		if(header == null || header.getValue() == null)
			return null;
		return header.getValue().trim();
	}
	
	public String getSingleHeaderValue(Http2HeaderName name) {
		Http2Header header = getHeaderLookupStruct().getHeader(name);
		if(header == null || header.getValue() == null) 
			return null;
		return header.getValue().trim();		
	}
	
	public boolean isEndOfStream() {
		return endOfStream;
	}
	public void setEndOfStream(boolean lastPartOfResponse) {
		this.endOfStream = lastPartOfResponse;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (endOfStream ? 1231 : 1237);
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((priorityDetails == null) ? 0 : priorityDetails.hashCode());
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
		Http2Headers other = (Http2Headers) obj;
		if (endOfStream != other.endOfStream)
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (priorityDetails == null) {
			if (other.priorityDetails != null)
				return false;
		} else if (!priorityDetails.equals(other.priorityDetails))
			return false;
		if (streamId != other.streamId)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String type = getClass().getSimpleName();
		return type+"[streamId=" + streamId + ", endStream=" + endOfStream + ", headerList="
				+ headers + ", priorityDetails=" + getPriorityDetails() + "]";
	}

}
