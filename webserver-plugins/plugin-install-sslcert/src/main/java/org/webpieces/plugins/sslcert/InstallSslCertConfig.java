package org.webpieces.plugins.sslcert;

public class InstallSslCertConfig {

	private String providerLocation;

	public InstallSslCertConfig(String providerLocation) {
		super();
		this.providerLocation = providerLocation;
	}

	public String getProviderLocation() {
		return providerLocation;
	}

}
