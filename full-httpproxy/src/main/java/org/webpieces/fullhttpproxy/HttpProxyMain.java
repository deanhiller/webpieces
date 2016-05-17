package org.webpieces.fullhttpproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpproxy.api.HttpProxy;
import org.webpieces.httpproxy.api.HttpProxyFactory;
import org.webpieces.httpproxy.api.ProxyConfig;

public class HttpProxyMain {

	private static final Logger log = LoggerFactory.getLogger(HttpProxyMain.class);
	
	public static void main(String[] args) {
		
		try {
			ProxyConfig config = new ProxyConfig();
			config.setForceAllConnectionToHttps(true);
			HttpProxy proxy = HttpProxyFactory.createHttpProxy("main", null, config);
			
			proxy.start();
		} catch(Exception e) {
			log.warn("excpeiton", e);
		}
	}
}
