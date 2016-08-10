package org.webpieces.webserver.api;

import org.webpieces.templating.api.HtmlTagLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TagOverridesModule implements Module {

	private Class<? extends HtmlTagLookup> override;

	public TagOverridesModule(Class<? extends HtmlTagLookup> override) {
		this.override = override;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(HtmlTagLookup.class).to(override).asEagerSingleton();
	}
	
}
