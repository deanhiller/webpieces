package org.webpieces.nio.api;

import javax.inject.Inject;
import javax.inject.Named;

public class SSLConfiguration {

	public static final String BACKEND_SSL = "backendSsl";
	
	private SSLEngineFactory httpsSslEngineFactory;
	private SSLEngineFactory backendSslEngineFactory;

	@Inject
	public SSLConfiguration(
		@Nullable SSLEngineFactory httpsSslEngineFactory, 
		@Nullable @Named(BACKEND_SSL) SSLEngineFactory backendSslEngineFactory
	) {
		super();
		this.httpsSslEngineFactory = httpsSslEngineFactory;
		this.backendSslEngineFactory = backendSslEngineFactory;
	}

	public SSLEngineFactory getHttpsSslEngineFactory() {
		return httpsSslEngineFactory;
	}

	public SSLEngineFactory getBackendSslEngineFactory() {
		return backendSslEngineFactory;
	}

}
