package org.webpieces.util.net;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

public class URLEncoder {

	/**
	 * need do convert all to taking a charset in configuration
	 */
	public static String encode(String value, Charset charset) {
		try {
			return java.net.URLEncoder.encode(value, charset.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * need do convert all to taking a charset in configuration
	 */
	public static String decode(String value, Charset charset) {
		try {
			return URLDecoder.decode(value, charset.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
		
}
