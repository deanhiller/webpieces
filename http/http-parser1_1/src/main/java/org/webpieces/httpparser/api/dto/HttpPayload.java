package org.webpieces.httpparser.api.dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

public abstract class HttpPayload {
	
	private static final DataWrapper EMPTY_WRAPPER = DataWrapperGeneratorFactory.createDataWrapperGenerator().emptyWrapper();
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
	public HttpLastChunk getLastHttpChunk() {
		if(getMessageType() == HttpMessageType.LAST_CHUNK)
			return (HttpLastChunk)this;
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
	
	/**
	 * This is true only if this is a response OR request with a
	 * Transfer-encoding header of chunked so the client will know if there are
	 * incoming HttpChunks after the initial message
	 * @return
	 */
	public abstract boolean isHasChunkedTransferHeader();
	
	/**
	 * convenience method for non-null body that will be 0 bytes if it was null
	 * @return
	 */
	public DataWrapper getBodyNonNull() {
		if(body == null)
			return EMPTY_WRAPPER;
		return body;
	}
}
