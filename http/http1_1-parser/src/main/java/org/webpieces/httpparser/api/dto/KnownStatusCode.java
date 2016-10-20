package org.webpieces.httpparser.api.dto;

import java.util.HashMap;
import java.util.Map;

public enum KnownStatusCode {

	HTTP_100_CONTINUE(100, "Continue", HttpStatusType.Informational),
	HTTP_101_SWITCHING_PROTOCOLS(101, "Switching Protocols", HttpStatusType.Informational),
	
	HTTP_200_OK(200, "OK", HttpStatusType.Success), 
	
	HTTP_300_MULTIPLE_CHOICES(300, "Multiple Choices", HttpStatusType.Redirection),
	HTTP_301_MOVED_PERMANENTLY(301, "Moved Permanently", HttpStatusType.Redirection),
	HTTP_302_FOUND(302, "Found", HttpStatusType.Redirection),
	HTTP_303_SEEOTHER(303, "See Other", HttpStatusType.Redirection),

	HTTP_400_BADREQUEST(400, "Bad Request", HttpStatusType.ClientError),
	HTTP_401_UNAUTHORIZED(401, "Unauthorized", HttpStatusType.ClientError),
	HTTP_404_NOTFOUND(404, "Not Found", HttpStatusType.ClientError),
	
	HTTP_408_REQUEST_TIMEOUT(408, "Request Timeout", HttpStatusType.ClientError),

	HTTP_413_PAYLOAD_TOO_LARGE(413, "Payload Too Large", HttpStatusType.ClientError),
	HTTP_431_REQUEST_HEADERS_TOO_LARGE(431, "Request Header Fields Too Large", HttpStatusType.ClientError),
	
	HTTP_500_INTERNAL_SVR_ERROR(500, "Internal Server Error", HttpStatusType.ServerError),  
	
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
