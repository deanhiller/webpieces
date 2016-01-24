package com.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpRequest {

	private RequestLine requestLine;
	
	private List<Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Headers headersStruct;

	public RequestLine getRequestLine() {
		return requestLine;
	}

	public void setRequestLine(RequestLine requestLine) {
		this.requestLine = requestLine;
	}
	
	/**
	 * Order of HTTP Headers matters for Headers with the same key
	 * 
	 * @param headers
	 */
	public List<Header> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	public void addHeader(Header header) {
		headers.add(header);
		headersStruct.addHeader(header);
	}

	/** 
	 * 
	 * @return
	 */
	public Headers getHeaderLookupStruct() {
		return headersStruct;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((requestLine == null) ? 0 : requestLine.hashCode());
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
		HttpRequest other = (HttpRequest) obj;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (requestLine == null) {
			if (other.requestLine != null)
				return false;
		} else if (!requestLine.equals(other.requestLine))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "" + requestLine;
	}

}
