package org.webpieces.templating.impl;

import java.io.Writer;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateInfo;

import groovy.lang.Binding;

public class TemplateImpl implements Template {

	private Class<?> compiledTemplate;
	private HtmlTagLookup tagLookup;

	public TemplateImpl(HtmlTagLookup tagLookup, Class<?> compiledTemplate) {
		this.tagLookup = tagLookup;
		this.compiledTemplate = compiledTemplate;
	}

	@Override
	public TemplateInfo run(Map<String, Object> args, Writer out) {
		
		Binding binding = new Binding(args);
		binding.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, out);

		GroovyTemplateSuperclass t = (GroovyTemplateSuperclass) InvokerHelper.createScript(compiledTemplate, binding);
		
		t.initialize(GroovyTemplateSuperclass.ESCAPE_HTML_FORMATTER, tagLookup);

		t.run();
		
		return new TemplateInfoImpl(t);
	}

}
