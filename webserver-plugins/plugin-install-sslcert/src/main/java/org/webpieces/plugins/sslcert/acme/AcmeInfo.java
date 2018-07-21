package org.webpieces.plugins.sslcert.acme;

import java.net.URI;
import java.net.URL;

public class AcmeInfo {

	private URI termsOfServiceUri;
	private URL website;
	
	public AcmeInfo(URI termsOfServiceUri, URL website) {
		super();
		this.termsOfServiceUri = termsOfServiceUri;
		this.website = website;
	}
	public URI getTermsOfServiceUri() {
		return termsOfServiceUri;
	}
	public URL getWebsite() {
		return website;
	}

}
