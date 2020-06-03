package com.webpieces.http2.api.subparsers;

import com.webpieces.http2.impl.subparsers.HeaderPriorityParserImpl;

public class SubparserFactory {
    
	public static HeaderPriorityParser createHeaderParser() {
		return new HeaderPriorityParserImpl();
	}
}
