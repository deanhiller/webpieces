package com.webpieces.hpack.api.subparsers;

import com.webpieces.hpack.impl.subparsers.HeaderPriorityParserImpl;

public class SubparserFactory {
    
	public static HeaderPriorityParser createHeaderParser() {
		return new HeaderPriorityParserImpl();
	}
}
