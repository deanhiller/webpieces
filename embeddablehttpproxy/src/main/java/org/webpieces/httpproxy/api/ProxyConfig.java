package org.webpieces.httpproxy.api;

public class ProxyConfig {

	private boolean isForceAllConnectionToHttps = false;
	private int numFrontendServerThreads = 10;
	private int numHttpClientThreads = 10;

	public boolean isForceAllConnectionToHttps() {
		return isForceAllConnectionToHttps;
	}

	public void setForceAllConnectionToHttps(boolean isForceAllConnectionToHttps) {
		this.isForceAllConnectionToHttps = isForceAllConnectionToHttps;
	}

	public int getNumFrontendServerThreads() {
		return numFrontendServerThreads;
	}

	public void setNumFrontendServerThreads(int numFrontendServerThreads) {
		this.numFrontendServerThreads = numFrontendServerThreads;
	}

	public int getNumHttpClientThreads() {
		return numHttpClientThreads;
	}

	public void setNumHttpClientThreads(int numHttpClientThreads) {
		this.numHttpClientThreads = numHttpClientThreads;
	}

}
