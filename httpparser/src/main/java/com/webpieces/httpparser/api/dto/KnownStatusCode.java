package com.webpieces.httpparser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum KnownStatusCode {

	HTTP100(100, "Continue", HttpStatusType.Informational),
	HTTP200(200, "OK", HttpStatusType.Success), 
	HTTP4XX(4xx, "Client Error", HttpStatusType.ClientError),
	HTTP500(500, "Server Error", HttpStatusType.ServerError),
	
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
	private HttpStatusType statusType;

	private KnownStatusCode(int code, String reason, HttpStatusType statusType) {
		this.code = code;
		this.reason = reason;
		this.statusType = statusType;
	}

	public int getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}
	
	public HttpStatusType getStatusType() {
		return statusType;
	}
	
	public static KnownStatusCode lookup(int code) {
		return codeToKnownStatus.get(code);
	}
}
