package org.webpieces.templating.api;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdTemplateModule implements Module {

	private TemplateConfig config;

	public ProdTemplateModule(TemplateConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateConfig.class).toInstance(config);
	}
}
