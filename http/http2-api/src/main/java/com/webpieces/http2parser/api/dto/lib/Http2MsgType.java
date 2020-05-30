package com.webpieces.http2parser.api.dto.lib;

public enum Http2MsgType {
	GOAWAY, 
	PING, 
	SETTINGS, 
	WINDOW_UPDATE, 
	DATA, 
	PUSH_PROMISE, 
	PRIORITY, 
	RST_STREAM, 
	UNKNOWN, 
	REQUEST_HEADERS, //A full request headers including continuation frames all put together 
	RESPONSE_HEADERS, //A full response headers including continuation frames all put together 
	TRAILING_HEADERS, //A full trailers headers including continuation frames all put together 
	APP_CANCEL_STREAM, //A messaage for apps only when a stream is cancelled due to exceptions or the app closing the stream
    ;

}
