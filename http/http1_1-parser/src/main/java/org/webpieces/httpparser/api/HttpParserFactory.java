package org.webpieces.httpparser.api;

import java.nio.charset.Charset;

import org.webpieces.data.api.BufferPool;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;
import org.webpieces.httpparser.impl.HttpParserImpl;
import org.webpieces.httpparser.impl.subparsers.HeaderPriorityParserImpl;
import org.webpieces.httpparser.impl.subparsers.HttpStatefulParserImpl;

import io.micrometer.core.instrument.MeterRegistry;

public class HttpParserFactory {

	public static final Charset ISO8859_1 = Charset.forName("ISO-8859-1");
	
	public static HttpParser createParser(String id, MeterRegistry metrics, BufferPool pool) {
		return createParser(id, metrics, pool, true);
	}
	
	/**
	 * 
	 * @param pool Purely to release ByteBuffers back to the pool and be released
	 * @return
	 */
	public static HttpParser createParser(String id, MeterRegistry metrics, BufferPool pool, boolean optimizeForBufferPool) {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new HttpParserImpl(id, metrics, pool, optimizeForBufferPool);
	}
	
	public static HttpStatefulParser createStatefulParser(String id, MeterRegistry metrics, BufferPool pool) {
		return new HttpStatefulParserImpl(createParser(id, metrics, pool, true));
	}
	
	public static HeaderPriorityParser createHeaderParser() {
		return new HeaderPriorityParserImpl();
	}
}
