package org.webpieces.httpparser.api.subparsers;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.ResponseCookie;
import org.webpieces.httpparser.api.dto.HttpRequest;

public interface HeaderPriorityParser {

	List<Locale> parseAcceptLangFromRequest(HttpRequest req);

	Map<String, String> parseCookiesFromRequest(HttpRequest req);

	List<AcceptType> parseAcceptFromRequest(HttpRequest req);
	
	Header createHeader(ResponseCookie cookie);
	
	<T> List<T> parsePriorityItems(String value, Function<String, T> parseFunction);

	List<String> parseAcceptEncoding(HttpRequest req);

}
