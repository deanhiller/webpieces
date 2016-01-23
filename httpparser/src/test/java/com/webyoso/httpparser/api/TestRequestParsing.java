package com.webyoso.httpparser.api;

import org.junit.Test;

public class TestRequestParsing {
	
	@Test
	public void testBasic() {
		
		HttpMethod method = HttpMethod.valueOf("OPTIONS");
		System.out.println("test="+method);
		
		HttpMethod method2 = HttpMethod.valueOf("nothing");
		System.out.println("test2="+method2);
		
		HttpParser parser = HttpParserFactory.createParser();
		
		HttpRequest request = new HttpRequest();
		request.setMethod(HttpMethod.POST);
		request.setVersion("1.1");
		
		byte[] buffer = parser.marshal(request);
		
		
		
		
	}

}
