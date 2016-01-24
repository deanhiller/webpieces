package com.webpieces.httpparser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum KnownHeaderName {
	//Per RFC, these are the General Header types...
	CACHE_CONTROL("Cache-Control", HeaderType.GENERAL),
	CONNECTION("Connection", HeaderType.GENERAL),
	DATE("Date", HeaderType.GENERAL),
	PRAGMA("Pragma", HeaderType.GENERAL),
	TRAILER("Trailer", HeaderType.GENERAL),
	TRANSFER_ENCODING("Transfer-Encoding", HeaderType.GENERAL),
	UPGRADE("Upgrade", HeaderType.GENERAL),
	VIA("Via", HeaderType.GENERAL),
	WARNING("Warning", HeaderType.GENERAL),

	//Per RFC, these are the Request Header types
	ACCEPT("Accept", HeaderType.REQUEST),
	ACCEPT_CHARSET("Accept-Charset", HeaderType.REQUEST),
	ACCEPT_ENCODING("Accept-Encoding", HeaderType.REQUEST),
	ACCEPT_LANGUAGE("Accept-Language", HeaderType.REQUEST),
	AUTHORIZATION("Authorization", HeaderType.REQUEST),
	EXPECT("Expect", HeaderType.REQUEST),
	FROM("From", HeaderType.REQUEST),
	HOST("Host", HeaderType.REQUEST),
	IF_MATCH("If-Match", HeaderType.REQUEST),
	IF_MODIFIED_SINCE("If-Modified-Since", HeaderType.REQUEST),
	IF_NONE_MATCH("If-None-Match", HeaderType.REQUEST),
	IF_RANGE("If-Range", HeaderType.REQUEST),
	IF_UNMODIFIED_SINCE("If-Unmodified-Since", HeaderType.REQUEST),
	MAX_FORWARDS("Max-Forwards", HeaderType.REQUEST),
	PROXY_AUTHORIZATION("Proxy-Authorization", HeaderType.REQUEST),
	RANGE("Range", HeaderType.REQUEST),
	REFERER("Referer", HeaderType.REQUEST),
	TE("TE", HeaderType.REQUEST),
	USER_AGENT("User-Agent", HeaderType.REQUEST),
	
	//Per RFC, these are the Response Header types
	ACCEPT_RANGES("Accept-Ranges", HeaderType.RESPONSE),
	AGE("Age", HeaderType.RESPONSE),
	ETAG("ETag", HeaderType.RESPONSE),
	LOCATION("Location", HeaderType.RESPONSE),
	PROXY_AUTHENTICATE("Proxy-Authenticate", HeaderType.RESPONSE),
	RETRY_AFTER("Retry-After", HeaderType.RESPONSE),
	SERVER("Server", HeaderType.RESPONSE),
	VARY("Vary", HeaderType.RESPONSE),
	WWW_AUTHENTICATE("WWW-Authenticate", HeaderType.RESPONSE)
	;

	private static Map<String, KnownHeaderName> lookup = new HashMap<>();
	
	static {
		for(KnownHeaderName name : KnownHeaderName.values()) {
			lookup.put(name.getHeaderName(), name);
		}
	}
	
	private String name;
	private HeaderType type;
	
	private KnownHeaderName(String name, HeaderType type) {
		this.name = name;
		this.type = type;
	}
	
	public String getHeaderName() {
		return name;
	}

	public HeaderType getHeaderType() {
		return type;
	}
	
	public static KnownHeaderName lookup(String name) {
		return lookup.get(name);
	}
}
