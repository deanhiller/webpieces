package org.webpieces.plugin.secure.sslcert;

public class InstallSslCertConfig {

	private String providerLocation;
	private PortType httpsPortType = PortType.HTTPS;
	private PortType backendPortType = PortType.HTTPS;

	public InstallSslCertConfig(String providerLocation) {
		super();
		this.providerLocation = providerLocation;
	}

	public String getProviderLocation() {
		return providerLocation;
	}

	public PortType getHttpsPortType() {
		return httpsPortType;
	}

	public PortType getBackendType() {
		return backendPortType;
	}

}
