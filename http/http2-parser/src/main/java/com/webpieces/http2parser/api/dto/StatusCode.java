package com.webpieces.http2parser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {

	HTTP_100_CONTINUE(100, "Continue", StatusType.Informational),
	HTTP_101_SWITCHING_PROTOCOLS(101, "Switching Protocols", StatusType.Informational),
	
	HTTP_200_OK(200, "OK", StatusType.Success), 
	
	HTTP_300_MULTIPLE_CHOICES(300, "Multiple Choices", StatusType.Redirection),
	HTTP_301_MOVED_PERMANENTLY(301, "Moved Permanently", StatusType.Redirection),
	HTTP_302_FOUND(302, "Found", StatusType.Redirection),
	HTTP_303_SEEOTHER(303, "See Other", StatusType.Redirection),

	HTTP_400_BADREQUEST(400, "Bad Request", StatusType.ClientError),
	HTTP_401_UNAUTHORIZED(401, "Unauthorized", StatusType.ClientError),
	HTTP_404_NOTFOUND(404, "Not Found", StatusType.ClientError),
	
	HTTP_408_REQUEST_TIMEOUT(408, "Request Timeout", StatusType.ClientError),

	HTTP_413_PAYLOAD_TOO_LARGE(413, "Payload Too Large", StatusType.ClientError),
	HTTP_431_REQUEST_HEADERS_TOO_LARGE(431, "Request Header Fields Too Large", StatusType.ClientError),
	
	HTTP_500_INTERNAL_SVR_ERROR(500, "Internal Server Error", StatusType.ServerError),  
	
	//TODO: Fill the rest in..
	;
	
	private static Map<Integer, StatusCode> codeToKnownStatus = new HashMap<>();
	
	static {
		for(StatusCode status : StatusCode.values()) {
			codeToKnownStatus.put(status.getCode(), status);
		}
	}
	
	private int code;
	private String reason;
	private StatusType statusType;

	StatusCode(int code, String reason, StatusType statusType) {
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
	
	public String getCodeString() {
		return ""+code;
	}
	
	public StatusType getStatusType() {
		return statusType;
	}
	
	public static StatusCode lookup(int code) {
		return codeToKnownStatus.get(code);
	}
}
