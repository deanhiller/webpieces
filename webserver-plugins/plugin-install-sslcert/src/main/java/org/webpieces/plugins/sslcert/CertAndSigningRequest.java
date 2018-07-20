package org.webpieces.plugins.sslcert;

import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.util.CSRBuilder;

public class CertAndSigningRequest {

	private CSRBuilder signingRequest;
	private Certificate finalCertificate;

	public CertAndSigningRequest(CSRBuilder signingRequest, Certificate finalCertificate) {
		this.signingRequest = signingRequest;
		this.finalCertificate = finalCertificate;
	}

	public CSRBuilder getSigningRequest() {
		return signingRequest;
	}

	public Certificate getFinalCertificate() {
		return finalCertificate;
	}
	
}
