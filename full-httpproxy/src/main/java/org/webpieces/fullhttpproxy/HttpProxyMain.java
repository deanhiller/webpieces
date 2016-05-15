package org.webpieces.fullhttpproxy;

import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.HttpProxyFactory;
import org.webpieces.httpproxy.api.ProxyConfig;

public class HttpProxyMain {

	public static void main(String[] args) {
		ProxyConfig config = new ProxyConfig();
		config.setForceAllConnectionToHttps(true);
		HttpProxy proxy = HttpProxyFactory.createHttpProxy("main", null, config);
		
		proxy.start();
	}
}
