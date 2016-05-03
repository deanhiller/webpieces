package com.webpieces.httpparser.api;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.UrlInfo;


public class TestUriParsing {

	@Test
	public void testBasicUrl() {
		HttpUri uri = new HttpUri("http://www.google.com:8080/there/is/cool?at=this&some=that");
		UrlInfo urlInfo = uri.getHostPortAndType();
		Assert.assertEquals("http", urlInfo.getPrefix());
		Assert.assertEquals("www.google.com", urlInfo.getHost());
		Assert.assertEquals(new Integer(8080), urlInfo.getPort());
	}
	@Test
	public void testSlash() {
		HttpUri uri = new HttpUri("/");
		
		UrlInfo urlInfo = uri.getHostPortAndType();
		Assert.assertEquals(null, urlInfo.getPrefix());
		Assert.assertEquals(null, urlInfo.getHost());
		Assert.assertEquals(null, urlInfo.getPort());
	}
}
