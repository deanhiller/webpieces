package org.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;

public abstract class HttpMessage extends HttpPayload {

	protected List<Header> headers = new ArrayList<>();
	//Convenience structure that further morphs the headers into a Map that can
	//be looked up by key.
	private transient Headers headersStruct = new Headers();
	
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
	
	public boolean isHasChunkedTransferHeader() {
		//need to account for a few Transfer Encoding headers
		Header header = headersStruct.getLastInstanceOfHeader(KnownHeaderName.TRANSFER_ENCODING);
		if(header != null && "chunked".equals(header.getValue()))
			return true;
		return false;
	}
}
