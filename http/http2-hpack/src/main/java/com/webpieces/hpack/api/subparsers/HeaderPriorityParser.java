package com.webpieces.hpack.api.subparsers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public interface HeaderPriorityParser {

	List<Locale> parseAcceptLangFromRequest(Http2Headers req);

	Map<String, String> parseCookiesFromRequest(Http2Headers req);

	List<AcceptType> parseAcceptFromRequest(Http2Headers req);
	
	Http2Header createHeader(ResponseCookie cookie);
	
	<T> List<T> parsePriorityItems(String value, Function<String, T> parseFunction);

	List<String> parseAcceptEncoding(Http2Headers req);

}
