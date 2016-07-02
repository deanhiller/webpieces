package org.webpieces.templating.api;

import org.webpieces.templating.impl.TemplateEngineImpl;

import com.google.inject.ImplementedBy;

@ImplementedBy(TemplateEngineImpl.class)
public interface TemplateEngine {

	public Template createTemplate(String className, String source);
}
