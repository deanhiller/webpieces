package org.webpieces.templating.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateResult;

import groovy.lang.Binding;

public class TemplateImpl implements Template {

	private Class<?> compiledTemplate;
	private HtmlTagLookup tagLookup;

	public TemplateImpl(HtmlTagLookup tagLookup, Class<?> compiledTemplate) {
		this.tagLookup = tagLookup;
		this.compiledTemplate = compiledTemplate;
	}

	@Override
	public TemplateResult run(Map<String, Object> args, Map<?, ?> templateProps, ReverseUrlLookup urlLookup) {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		Binding binding = new Binding(args);
		binding.setProperty(GroovyTemplateSuperclass.OUT_PROPERTY_NAME, writer);

		GroovyTemplateSuperclass t = (GroovyTemplateSuperclass) InvokerHelper.createScript(compiledTemplate, binding);		
		
		t.initialize(GroovyTemplateSuperclass.ESCAPE_HTML_FORMATTER, tagLookup, templateProps, urlLookup);

		t.run();
		
		return new TemplateResultImpl(t, out.toString());
	}

}
