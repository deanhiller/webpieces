package com.webpieces.httpparser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum KnownStatusCode {

	HTTP100(100, "Continue"),
	HTTP200(200, "OK"),
	
	//TODO: Fill the rest in..
	;
	
	private static Map<Integer, KnownStatusCode> codeToKnownStatus = new HashMap<>();
	
	static {
		for(KnownStatusCode status : KnownStatusCode.values()) {
			codeToKnownStatus.put(status.getCode(), status);
		}
	}
	
	private int code;
	private String reason;

	private KnownStatusCode(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}

	public int getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}
	
	public static KnownStatusCode lookup(int code) {
		return codeToKnownStatus.get(code);
	}
}
