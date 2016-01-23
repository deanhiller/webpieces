package com.webyoso.httpparser.api;

import com.webyoso.httpparser.impl.HttpParserImpl;

public class HttpParserFactory {

	public static HttpParser createParser() {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new HttpParserImpl();
	}
}
