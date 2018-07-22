package org.webpieces.plugins.documentation;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class DocumentationModule implements Module {

	private DocumentationConfig config;

	public DocumentationModule(DocumentationConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		//create empty set in case no other plugins are installed so we work with 0 plugins though that is quite boring
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder, BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(DocumentationGuiDescriptor.class);

	    binder.bind(DocumentationConfig.class).toInstance(config);
	}

}
