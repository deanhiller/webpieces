package org.webpieces.httpparser.api.dto;

public abstract class HttpPayload {

	public abstract HttpMessageType getMessageType();
	
	public HttpRequest getHttpRequest() {
		if(getMessageType() == HttpMessageType.REQUEST)
			return (HttpRequest)this;
		throw new ClassCastException("This is not a request and is="+this);
	}
	public HttpResponse getHttpResponse() {
		if(getMessageType() == HttpMessageType.RESPONSE)
			return (HttpResponse)this;
		throw new ClassCastException("This is not a response and is="+this);
	}
	public HttpChunk getHttpChunk() {
		if(getMessageType() == HttpMessageType.CHUNK || getMessageType() == HttpMessageType.LAST_CHUNK)
			return (HttpChunk)this;
		throw new ClassCastException("This is not a HttpChunk and is="+this);
	}	
	
	public HttpData getHttpData() {
		if(getMessageType() == HttpMessageType.DATA || getMessageType() == HttpMessageType.LAST_DATA)
			return (HttpData)this;
		throw new ClassCastException("This is not a HttpData and is="+this);
	}	
	
	public HttpLastChunk getLastHttpChunk() {
		if(getMessageType() == HttpMessageType.LAST_CHUNK)
			return (HttpLastChunk)this;
		return null;
	}
	

}
