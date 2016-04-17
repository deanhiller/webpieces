package org.webpieces.fullhttpproxy;

import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.HttpProxyFactory;

public class HttpProxyMain {

	public static void main(String[] args) {
		HttpProxy proxy = HttpProxyFactory.createHttpProxy("main", null);
		
		proxy.start();
	}
}
