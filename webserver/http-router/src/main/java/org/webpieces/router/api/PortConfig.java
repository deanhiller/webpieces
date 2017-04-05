package org.webpieces.router.api;

public class PortConfig {

	private int httpPort;
	private int httpsPort;

	public PortConfig(int httpPort, int httpsPort) {
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public int getHttpsPort() {
		return httpsPort;
	}

}
