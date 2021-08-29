package org.webpieces.http;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {

    HTTP_100_CONTINUE(100, "Continue", StatusType.INFORMATIONAL),
    HTTP_101_SWITCHING_PROTOCOLS(101, "Switching Protocols", StatusType.INFORMATIONAL),

    HTTP_200_OK(200, "OK", StatusType.SUCCESS),
    HTTP_201_CREATED(201, "Created", StatusType.SUCCESS),
    HTTP_202_ACCEPTED(202, "Accepted", StatusType.SUCCESS),
    HTTP_203_NON_AUTHORITATIVE_INFO(203, "Non-Authoritative Information", StatusType.SUCCESS),
    HTTP_204_NO_CONTENT(204, "No Content", StatusType.SUCCESS),

    HTTP_300_MULTIPLE_CHOICES(300, "Multiple Choices", StatusType.REDIRECTION),
    HTTP_301_MOVED_PERMANENTLY(301, "Moved Permanently", StatusType.REDIRECTION),
    /** @deprecated Use either {@link #HTTP_303_SEE_OTHER} or {@link #HTTP_307_TEMPORARY_REDIRECT}. */
    @Deprecated
    HTTP_302_FOUND(302, "Found", StatusType.REDIRECTION),
    HTTP_303_SEE_OTHER(303, "See Other", StatusType.REDIRECTION),
    HTTP_304_NOT_MODIFIED(304, "Not Modified", StatusType.REDIRECTION),
    HTTP_305_USE_PROXY(305, "Use Proxy", StatusType.REDIRECTION),
    HTTP_307_TEMPORARY_REDIRECT(307, "Temporary Redirect", StatusType.REDIRECTION),
    HTTP_308_PERMANENT_REDIRECT(308, "Permanent Redirect", StatusType.REDIRECTION),

    HTTP_400_BAD_REQUEST(400, "Bad Request", StatusType.CLIENT_ERROR),
    HTTP_401_UNAUTHORIZED(401, "Unauthorized", StatusType.CLIENT_ERROR),
    HTTP_402_PAYMENT_REQUIRED(402, "Payment Required", StatusType.CLIENT_ERROR),
    HTTP_403_FORBIDDEN(403, "Forbidden", StatusType.CLIENT_ERROR),
    HTTP_404_NOT_FOUND(404, "Not Found", StatusType.CLIENT_ERROR),
    HTTP_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed", StatusType.CLIENT_ERROR),
    HTTP_406_NOT_ACCEPTABLE(404, "Not Acceptable", StatusType.CLIENT_ERROR),

    HTTP_408_REQUEST_TIMEOUT(408, "Request Timeout", StatusType.CLIENT_ERROR),
    HTTP_409_CONFLICT(409, "Conflict", StatusType.CLIENT_ERROR),
    HTTP_410_GONE(410, "Gone", StatusType.CLIENT_ERROR),
    HTTP_411_LENGTH_REQUIRED(411, "Length Required", StatusType.CLIENT_ERROR),
    HTTP_412_PRECONDITION_FAILED(412, "Precondition Failed", StatusType.CLIENT_ERROR),
    HTTP_413_PAYLOAD_TOO_LARGE(413, "Payload Too Large", StatusType.CLIENT_ERROR),
    HTTP_414_URI_TOO_LONG(414, "URI Too Long", StatusType.CLIENT_ERROR),
    HTTP_415_UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", StatusType.CLIENT_ERROR),
    HTTP_417_EXPECTATION_FAILED(417, "Expectation Failed", StatusType.CLIENT_ERROR),
    HTTP_418_IM_A_TEAPOT(418, "I'm A Teapot", StatusType.CLIENT_ERROR),

    HTTP_425_TOO_EARLY(425, "Too Early", StatusType.CLIENT_ERROR),
    HTTP_428_PRECONDITION_REQUIRED(428, "Precondition Required", StatusType.CLIENT_ERROR),
    HTTP_429_TOO_MANY_REQUESTS(429, "Too many requests", StatusType.CLIENT_ERROR),
    HTTP_431_REQUEST_HEADERS_TOO_LARGE(431, "Request Header Fields Too Large", StatusType.CLIENT_ERROR),
    HTTP_451_UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons", StatusType.CLIENT_ERROR),

    //passthrough so upstream server knows to pass through BECAUSE upstream microservice turns 400 into
    //500 because it is his bug BUT if you send this it tells the upstream server the request came all the
    //way from a client far away and it is the client's fault
    HTTP_491_BAD_CUSTOMER_REQUEST(491, "Bad Customer Request", StatusType.CLIENT_ERROR),

    HTTP_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error", StatusType.SERVER_ERROR),
    HTTP_501_NOT_IMPLEMENTED(501, "Not Implemented", StatusType.SERVER_ERROR),
    HTTP_502_BAD_GATEWAY(502, "Bad Gateway", StatusType.SERVER_ERROR),
    HTTP_503_SERVICE_UNAVAILABLE(503, "Service Unavailable", StatusType.SERVER_ERROR),
    HTTP_504_GATEWAY_TIMEOUT(504, "Gateway Timeout", StatusType.SERVER_ERROR),
    HTTP_505_HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", StatusType.SERVER_ERROR)

    ;

    private static final Map<Integer, StatusCode> CODE_TO_STATUS = new HashMap<>();

    static {
        for(StatusCode status : StatusCode.values()) {
            CODE_TO_STATUS.put(status.getCode(), status);
        }
    }

    private final int code;
    private final String reason;
    private final StatusType statusType;

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

    public StatusType getStatusType() {
        return statusType;
    }

    public static StatusCode lookup(int code) {
        return CODE_TO_STATUS.get(code);
    }

    public enum StatusType {

		INFORMATIONAL,
		SUCCESS,
		REDIRECTION,
		CLIENT_ERROR,
		SERVER_ERROR

    }

}
