package org.webpieces.httpproxy.api;

import org.junit.Test;

public class TestHttpProxy {

	@Test
	public void testBasicProxy() {
		HttpProxy proxy = HttpProxyFactory.createHttpProxy("myproxy", null);
		
		
		proxy.start();
	}
}
