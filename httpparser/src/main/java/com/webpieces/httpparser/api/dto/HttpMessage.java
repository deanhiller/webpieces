package com.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.common.Header;

public abstract class HttpMessage {

	protected List<Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Headers headersStruct = new Headers();
	
	private DataWrapper body;
	
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
	
	public abstract HttpMessageType getMessageType();
	
	public HttpRequest getHttpRequest() {
		if(getMessageType() == HttpMessageType.REQUEST)
			return (HttpRequest)this;
		return null;
	}
	public HttpResponse getHttpResponse() {
		if(getMessageType() == HttpMessageType.RESPONSE)
			return (HttpResponse)this;
		return null;
	}
	
	public void setBody(DataWrapper data) {
		this.body = data;
	}
	
	public DataWrapper getBody() {
		return body;
	}
}
