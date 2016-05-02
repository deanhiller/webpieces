package com.webpieces.httpparser.api.dto;

import com.webpieces.data.api.DataWrapper;

public abstract class HttpMessage {
	
	private DataWrapper body;

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
	public HttpChunk getHttpChunk() {
		if(getMessageType() == HttpMessageType.CHUNK)
			return (HttpChunk)this;
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
	 * @return
	 */
	public DataWrapper getBody() {
		return body;
	}
}
