package org.webpieces.templating.api;

import org.webpieces.templating.impl.DevTemplateService;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DevTemplateModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateService.class).to(DevTemplateService.class).asEagerSingleton();
	}
}
