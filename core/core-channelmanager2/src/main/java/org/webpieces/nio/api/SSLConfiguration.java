package org.webpieces.nio.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * This is a bit complicated.  This class is created during webpieces webserver guice construction since
 * the core webserver needs it.  However, the ssl plugin wants to fill it in IF the plugin exists at
 * application creation time which is after webserver creation but it is before we startup the ports
 * so this class can be filled in at a later time by a plugin or by the application developer
 * 
 */
@Singleton
public class SSLConfiguration {

	public static final String BACKEND_SSL = "backendSsl";
	
	private SSLEngineFactory httpsSslEngineFactory;
	private SSLEngineFactory backendSslEngineFactory;

	@Inject
	public SSLConfiguration(
			@Nullable SSLEngineFactory httpsSslEngineFactory, 
			@Nullable @Named(BACKEND_SSL) SSLEngineFactory backendSslEngineFactory) {
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
