package com.webpieces.http2.api.dto.lowlevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Http2Method {

	OPTIONS("OPTIONS"),
	GET("GET"),
	HEAD("HEAD"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	TRACE("TRACE"),
	CONNECT("CONNECT");
	
	private static Map<String, Http2Method> codeToKnownStatus = new HashMap<>();
	
	static {
		for(Http2Method status : Http2Method.values()) {
			codeToKnownStatus.put(status.getCode(), status);
		}
	}
	
	private String code;
	
	Http2Method(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	public static Http2Method lookup(String method) {
		return codeToKnownStatus.get(method);
	}
	
	public static Set<Http2Method> getAll() {
		Collection<Http2Method> values = codeToKnownStatus.values();
		Set<Http2Method> methods = new HashSet<>();
		methods.addAll(values);
		return methods;
	}
}
