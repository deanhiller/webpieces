package com.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webpieces.data.api.DataWrapper;
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
	
	/**
	 * 
	 * @param data
	 */
	public void setBody(DataWrapper data) {
		this.body = data;
	}
	
	/**
	 * This is not final.  We need consider very large bodies along with bytes streaming in 
	 * off the socket.  (we do have the ability to unregisterForReads to slow down the far
	 * end if needed)....This is sort of where we need an adapter layer between 
	 * channelmanager and the http parser.
	 * @return
	 */
	public DataWrapper getBody() {
		return body;
	}
}
