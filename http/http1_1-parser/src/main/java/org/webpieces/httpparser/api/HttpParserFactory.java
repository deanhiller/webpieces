package org.webpieces.httpparser.api;

import java.nio.charset.Charset;

import org.webpieces.data.api.BufferPool;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;
import org.webpieces.httpparser.impl.HttpParserImpl;
import org.webpieces.httpparser.impl.subparsers.HeaderPriorityParserImpl;
import org.webpieces.httpparser.impl.subparsers.HttpStatefulParserImpl;

public class HttpParserFactory {

	public static final Charset iso8859_1 = Charset.forName("ISO-8859-1");
	/**
	 * 
	 * @param pool Purely to release ByteBuffers back to the pool and be released
	 * @return
	 */
	public static HttpParser createParser(BufferPool pool) {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new HttpParserImpl(pool);
	}
	
	public static HttpStatefulParser createStatefulParser(BufferPool pool) {
		return new HttpStatefulParserImpl(createParser(pool));
	}
	
	public static HeaderPriorityParser createHeaderParser() {
		return new HeaderPriorityParserImpl();
	}
}
