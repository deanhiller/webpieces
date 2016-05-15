package org.webpieces.httpproxy.api;

public class ProxyConfig {

	private boolean isForceAllConnectionToHttps = false;

	public boolean isForceAllConnectionToHttps() {
		return isForceAllConnectionToHttps;
	}

	public void setForceAllConnectionToHttps(boolean isForceAllConnectionToHttps) {
		this.isForceAllConnectionToHttps = isForceAllConnectionToHttps;
	}
	
}
