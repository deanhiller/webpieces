package org.webpieces.templating.impl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.HtmlTagLookup;
import org.webpieces.templating.api.ReverseUrlLookup;
import org.webpieces.templating.impl.html.EscapeHTMLFormatter;
import org.webpieces.templating.impl.html.NullFormatter;

import groovy.lang.Closure;
import groovy.lang.Script;

public abstract class GroovyTemplateSuperclass extends Script {

	public static String ESCAPER_PROPERTY_NAME = "__escaper";
	public static String OUT_PROPERTY_NAME = "__out";
	public static final EscapeHTMLFormatter ESCAPE_HTML_FORMATTER = new EscapeHTMLFormatter();
	private static final NullFormatter NULL_FORMATTER = new NullFormatter();
	
	private EscapeCharactersFormatter formatter;
	private HtmlTagLookup tagLookup;
	@SuppressWarnings("rawtypes")
	private Map templateProperties;
	private String superTemplateFilePath;
	private ReverseUrlLookup urlLookup;
	
    public void initialize(EscapeCharactersFormatter f, HtmlTagLookup tagLookup, Map<?, ?> templateProps, ReverseUrlLookup urlLookup) {
    	formatter = f;
    	this.tagLookup = tagLookup;
    	this.templateProperties = templateProps;
    	this.urlLookup = urlLookup;
    }
    
    protected void installNullFormatter() {
    	formatter = NULL_FORMATTER;
    }
    
    protected void installHtmlFormatter() {
    	formatter = ESCAPE_HTML_FORMATTER;
    }

    public String useFormatter(Object val) {
    	return formatter.format(val);
    }

    protected void runTag(String tagName, Map<?, ?> args, Closure<?> closure, String srcLocation) {
    	HtmlTag tag = tagLookup.lookup(tagName);
    	PrintWriter writer = (PrintWriter) getProperty(OUT_PROPERTY_NAME);
    	tag.runTag(args, closure, writer, this, srcLocation);
    }

	@SuppressWarnings("unchecked")
	public void putTemplateProperty(Object key, Object val) {
		templateProperties.put(key, val);
	}
	
	public Object getTemplateProperty(Object key) {
		return templateProperties.get(key);
	}
	
	public void setSuperTemplateFilePath(String path) {
		this.superTemplateFilePath = path;
	}

	public String getSuperTemplateFilePath() {
		return superTemplateFilePath;
	}

	public Map<?, ?> getTemplateProperties() {
		return templateProperties;
	}
	
	public String fetchUrl(String routeId, Map<?, ?> args) {
		Map<String, String> urlParams = new HashMap<>();
		for(Map.Entry<?, ?> entry : args.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();
			if(value == null)
				throw new IllegalArgumentException("Cannot use null param in a url for param name="+key);
			urlParams.put(key, value.toString());
		}
		return urlLookup.fetchUrl(routeId, urlParams);
	}
}
