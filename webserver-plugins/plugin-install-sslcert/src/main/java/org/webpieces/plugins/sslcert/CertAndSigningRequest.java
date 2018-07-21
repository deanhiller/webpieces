package org.webpieces.plugins.sslcert;

import java.security.cert.X509Certificate;
import java.util.List;

public class CertAndSigningRequest {

	private String csr;
	private List<X509Certificate> certChain;

	public CertAndSigningRequest(String csr, List<X509Certificate> certChain) {
		this.csr = csr;
		this.certChain = certChain;
	}

	public String getCsr() {
		return csr;
	}

	public List<X509Certificate> getCertChain() {
		return certChain;
	}

}
