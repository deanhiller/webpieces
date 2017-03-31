package org.webpieces.util.net;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class URLEncoder {

	/**
	 * need do convert all to taking a charset in configuration
	 */
	@Deprecated
	public static String encode(String value) {
		try {
			return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * need do convert all to taking a charset in configuration
	 */
	@Deprecated
	public static String decode(String value) {
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
		
}
