package org.webpieces.plugins.sslcert;

import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class InstallSslCertModule extends AbstractModule {

	private InstallSslCertConfig config;

	//Variables to consider in this plugin
	// 1. production with an actual domain on first time startup and need cert
	// 2. production with an actual domain on second tie startup and have cert
	// 3. test cases
	// 4. local development startup and population of an empty database.
	//MUST test all those situations when modifying this plugin 'manually' :( :( :(
	
	public InstallSslCertModule(InstallSslCertConfig config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder(), BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(InstallSslCertGuiDescriptor.class);
	    
	    binder().bind(InstallSslCertConfig.class).toInstance(config);
	    
	    PortType type = config.getHttpsPortType();
	    PortType backEndType = config.getBackendType();
	    
	    if(type == PortType.HTTPS)
	    	 binder().bind(SSLEngineFactory.class).to(WebSSLFactory.class);
	    	
	    if(backEndType == PortType.HTTPS)
	    	 binder().bind(SSLEngineFactory.class).annotatedWith(Names.named(SSLConfiguration.BACKEND_SSL)).to(WebSSLFactory.class);
	    
	    
	}

}
