package org.webpieces.templating.impl;

import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;

public class TemplateServiceImpl implements TemplateService {

	@Override
	public Template loadTemplate(String packageStr, String templateClassName, String extension) {
		throw new UnsupportedOperationException("This is for production and precompiled templates and is not implemented yet");
	}

}
