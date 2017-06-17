package org.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.common.Header;

public class HttpLastChunk extends HttpChunk {

	protected List<Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Headers headersStruct = new Headers();
	
	@Override
	public boolean isEndOfData() {
		return true;
	}
	
	/**
	 * Order of HTTP Headers matters for Headers with the same key
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
	public HttpMessageType getMessageType() {
		return HttpMessageType.LAST_CHUNK;
	}

	
	
	@Override
	public void setBody(DataWrapper data) {
		if(data.getReadableSize() != 0)
			throw new IllegalArgumentException("Can't set body on HttpLastChunk according to http spec.  It must be size=0");
		super.setBody(data);
	}

	@Override
	public DataWrapper getBody() {
		return DataWrapperGeneratorFactory.EMPTY;
	}
	
	@Override
	public String createTrailer() {
		String lastPart = "";
		for(Header header : getHeaders()) {
			lastPart += header.getName()+": "+header.getValue()+TRAILER_STR;
		}
		
		lastPart += TRAILER_STR;
		
		return lastPart;
	}

}
