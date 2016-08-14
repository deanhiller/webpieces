package org.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;

public class Headers {

	private Map<String, List<Header>> headers = new HashMap<>();
	
	public Header getHeader(KnownHeaderName name) {
		return getHeader(name.getHeaderName().toLowerCase());
	}
	public List<Header> getHeaders(KnownHeaderName name) {
		List<Header> headers2 = getHeaders(name.getHeaderName().toLowerCase());
		if(headers2 == null)
			return new ArrayList<>();
		return headers2;
	}
	public Header getLastInstanceOfHeader(KnownHeaderName name) {
		List<Header> headers = getHeaders(name.getHeaderName().toLowerCase());
		if(headers == null)
			return null;
		else if(headers.size() == 0)
			return null;
		return headers.get(headers.size()-1);
	}
	
	public Header getHeader(String key) {
		List<Header> list = headers.get(key.toLowerCase());
		if(list == null)
			return null;
		else if(list.size() > 1)
			throw new IllegalStateException("There are two headers in this http request with key="+key+". use getHeaders method instead");
		else if(list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Header> getHeaders(String key) {
		return headers.get(key);
	}

	/**
	 * It is important that this is not exposed as the duplicate structure could get corrupted.
	 * Adding anything to this from a client does nothing so we don't want to allow this as the
	 * user would think that it would be marshalled out
	 */
	protected void addHeader(Header header) {
		List<Header> list = headers.get(header.getName());
		if(list == null) {
			list = new ArrayList<>();
			//Header names are not case sensitive while values are
			headers.put(header.getName().toLowerCase(), list);
		}
		list.add(header);
	}
	
}
