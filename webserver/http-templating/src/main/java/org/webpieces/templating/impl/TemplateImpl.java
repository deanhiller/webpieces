package org.webpieces.templating.impl;

import java.io.Writer;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.webpieces.templating.api.Template;

import groovy.lang.Binding;

public class TemplateImpl implements Template {

	private Class<?> compiledTemplate;

	public TemplateImpl(Class<?> compiledTemplate) {
		this.compiledTemplate = compiledTemplate;
	}

	@Override
	public void run(Map<String, Object> args, Writer out) {
		
		Binding binding = new Binding(args);
		binding.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, out);

		GroovyTemplateSuperclass t = (GroovyTemplateSuperclass) InvokerHelper.createScript(compiledTemplate, binding);
		
		t.initialize(GroovyTemplateSuperclass.ESCAPE_HTML_FORMATTER);

		t.run();
		
	}

}
