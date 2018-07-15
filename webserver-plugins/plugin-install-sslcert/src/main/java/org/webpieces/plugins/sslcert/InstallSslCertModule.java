package org.webpieces.plugins.sslcert;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class InstallSslCertModule extends AbstractModule {

	//Variables to consider in this plugin
	// 1. production with an actual domain on first time startup and need cert
	// 2. production with an actual domain on second tie startup and have cert
	// 3. test cases
	// 4. local development startup and population of an empty database.
	//MUST test all those situations when modifying this plugin 'manually' :( :( :(
	
	@Override
	protected void configure() {
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder(), BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(BackendInfoImpl.class);
	}

}
