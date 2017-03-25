package org.webpieces.templating.impl;

import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateResult;

import groovy.lang.Binding;

public class TemplateImpl implements Template {

	private Class<?> compiledTemplate;
	private HtmlTagLookup tagLookup;
	private RouterLookup urlLookup;

	public TemplateImpl(RouterLookup urlLookup, HtmlTagLookup tagLookup, Class<?> compiledTemplate) {
		this.urlLookup = urlLookup;
		this.tagLookup = tagLookup;
		this.compiledTemplate = compiledTemplate;
	}

	@Override
	public TemplateResult run(Map<String, Object> args, Map<Object, Object> templateProps) {
		Binding binding = new Binding(args);

		GroovyTemplateSuperclass t = (GroovyTemplateSuperclass) InvokerHelper.createScript(compiledTemplate, binding);		
		
		t.initialize(GroovyTemplateSuperclass.ESCAPE_HTML_FORMATTER, tagLookup, templateProps, urlLookup);

		t.run();
		
		return new TemplateResultImpl(t);
	}

}
