package org.webpieces.templating.impl;

import java.io.StringWriter;
import java.util.Map;

import org.webpieces.ctx.api.MissingPropException;
import org.webpieces.templating.api.TemplateService;

import groovy.lang.MissingPropertyException;

public abstract class AbstractTemplateService implements TemplateService {

	@Override
	public void loadAndRunTemplate(String templatePath, StringWriter out, Map<String, Object> pageArgs) {
		try {
			loadAndRunTemplateImpl(templatePath, out, pageArgs);
		} catch(MissingPropertyException e) {
			throw new MissingPropException(e);
		}
	}

	protected abstract void loadAndRunTemplateImpl(String templatePath, StringWriter out, Map<String, Object> pageArgs);

}
