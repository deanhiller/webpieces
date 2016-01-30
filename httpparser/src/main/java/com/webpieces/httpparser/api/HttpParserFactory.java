package com.webpieces.httpparser.api;

import com.webpieces.httpparser.impl.HttpParserImpl;
import com.webpieces.httpparser.impl.data.DataWrapperGeneratorImpl;

public class HttpParserFactory {

	public static HttpParser createParser() {
		//to get around verifydesign later AND enforce build breaks on design violations
		//like api depending on implementation, we need reflection here to create this
		//instance...
		return new HttpParserImpl();
	}
	
	public static DataWrapperGenerator createDataWrapperGenerator() {
		return new DataWrapperGeneratorImpl();
	}
}
