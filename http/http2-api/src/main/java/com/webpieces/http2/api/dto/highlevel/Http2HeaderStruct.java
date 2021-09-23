package com.webpieces.http2.api.dto.highlevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;

public class Http2HeaderStruct {

	private Map<String, List<Http2Header>> headers = new HashMap<>();
	
	public Http2Header getHeader(Http2HeaderName name) {
		return getHeader(name.getHeaderName().toLowerCase());
	}
	public List<Http2Header> getHeaders(Http2HeaderName name) {
		List<Http2Header> headers2 = getHeaders(name.getHeaderName().toLowerCase());
		if(headers2 == null)
			return new ArrayList<>();
		return headers2;
	}
	public Http2Header getLastInstanceOfHeader(Http2HeaderName name) {
		List<Http2Header> headers = getHeaders(name.getHeaderName().toLowerCase());
		if(headers == null)
			return null;
		else if(headers.size() == 0)
			return null;
		return headers.get(headers.size()-1);
	}
	
	public Http2Header getHeader(String key) {
		List<Http2Header> list = headers.get(key.toLowerCase());
		if(list == null)
			return null;
		else if(list.size() > 1)
			throw new IllegalStateException("There are two headers in this http request with key="+key+". use getHeaders method instead");
		else if(list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Http2Header> getHeaders(String key) {
		return headers.get(key.toLowerCase());
	}

	protected void addHeader(Http2Header header) {
//		char[] newVal = new char[header.getName().length()];
//		for (int i = 0; i < newVal.length; i++) {
//			char ch= header.getName().charAt(i);
//			newVal[i]= ch >= 'A' && ch <= 'Z' ? (char) (ch + 'a' - 'A') : ch;
//		}
		
		addHeader(header.getName().toLowerCase(), header);
	}
	/**
	 * It is important that this is not exposed as the duplicate structure could get corrupted.
	 * Adding anything to this from a client does nothing so we don't want to allow this as the
	 * user would think that it would be marshalled out
	 */
	protected void addHeader(String name, Http2Header header) {
		List<Http2Header> list = headers.get(name);
		if(list == null) {
			list = new ArrayList<>(10);
			//Header names are not case sensitive while values are
			headers.put(name, list);
		}
		list.add(header);
	}
	
}
