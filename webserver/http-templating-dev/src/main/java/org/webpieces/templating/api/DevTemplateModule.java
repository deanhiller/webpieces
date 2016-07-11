package org.webpieces.templating.api;

import org.webpieces.templating.impl.DevTemplateService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevTemplateModule implements Module {

	private TemplateCompileConfig config;

	public DevTemplateModule(TemplateCompileConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateService.class).to(DevTemplateService.class).asEagerSingleton();
		binder.bind(TemplateCompileConfig.class).toInstance(config);
	}
}
