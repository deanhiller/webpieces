package org.webpieces.templating.api;

import org.webpieces.templating.impl.TemplateEngineImpl;

public class TemplatingFactory {

	public static TemplateEngine create() {
		return new TemplateEngineImpl();
	}
}
