package org.webpieces.templating.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
	public TemplateResult run(Map<String, Object> args, Map<Object, Object> templateProps, ReverseUrlLookup urlLookup) {
		Binding binding = new Binding(args);

		GroovyTemplateSuperclass t = (GroovyTemplateSuperclass) InvokerHelper.createScript(compiledTemplate, binding);		
		
		List<ResourceBundle> bundles = new ArrayList<>();
		t.initialize(GroovyTemplateSuperclass.ESCAPE_HTML_FORMATTER, tagLookup, templateProps, urlLookup, bundles);

		t.run();
		
		return new TemplateResultImpl(t);
	}

}
