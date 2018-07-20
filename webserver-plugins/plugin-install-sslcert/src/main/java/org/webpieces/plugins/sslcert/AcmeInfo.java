package org.webpieces.plugins.sslcert;

import java.net.URI;
import java.net.URL;

import org.shredzone.acme4j.Metadata;

public class AcmeInfo {

	private Metadata metadata;
	private URI uri;
	private URL website;

	public AcmeInfo(Metadata metadata, URI uri, URL website) {
		this.metadata = metadata;
		this.uri = uri;
		this.website = website;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public URI getUri() {
		return uri;
	}

	public URL getWebsite() {
		return website;
	}
	
}
