package org.webpieces.templating.api;

import org.webpieces.templating.impl.DevTemplateCompileCallback;
import org.webpieces.templating.impl.DevTemplateService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevTemplateModule implements Module {

	private TemplateCompileConfig config;
	private CompileCallback callback;

	public DevTemplateModule(TemplateCompileConfig config) {
		this(config, new DevTemplateCompileCallback());
	}
	
	public DevTemplateModule(TemplateCompileConfig config, CompileCallback callback) {
		this.config = config;
		this.callback = callback;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateService.class).to(DevTemplateService.class).asEagerSingleton();
		binder.bind(TemplateCompileConfig.class).toInstance(config);
		binder.bind(CompileCallback.class).toInstance(callback);
	}
}
