package com.webpieces.httpparser.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Headers {

	private Map<String, List<Header>> headers = new HashMap<>();
	
	public Header getHeader(KnownHeaderName name) {
		return getHeader(name.getHeaderName());
	}
	public List<Header> getHeaders(KnownHeaderName name) {
		return getHeaders(name.getHeaderName());
	}
	
	public Header getHeader(String key) {
		List<Header> list = headers.get(key);
		if(list.size() > 1)
			throw new IllegalStateException("There are two headers in this http request with key="+key+". use getHeaders method instead");
		else if(list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Header> getHeaders(String key) {
		return headers.get(key);
	}

	protected void addHeader(Header header) {
		List<Header> list = headers.get(header.getName());
		if(list == null) {
			list = new ArrayList<>();
			headers.put(header.getName(), list);
		}
		list.add(header);
	}
	
}
