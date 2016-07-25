package org.webpieces.templating.impl;

import org.webpieces.templating.impl.html.EscapeHTMLFormatter;
import org.webpieces.templating.impl.html.NullFormatter;

import groovy.lang.Script;

public abstract class GroovyTemplateSuperclass extends Script {

	public static final String ESCAPER_PROPERTY_NAME = "__escaper";
	public static final String OUT_PROPERTY_NAME = "__out";
	public static final EscapeHTMLFormatter ESCAPE_HTML_FORMATTER = new EscapeHTMLFormatter();
	private static final NullFormatter NULL_FORMATTER = new NullFormatter();
	
	private EscapeCharactersFormatter formatter;
	
    public void initialize(EscapeCharactersFormatter f) {
    	formatter = f;
    }
    
    protected void installNullFormatter() {
    	formatter = NULL_FORMATTER;
    }
    
    protected void installHtmlFormatter() {
    	formatter = ESCAPE_HTML_FORMATTER;
    }

    protected String useFormatter(Object val) {
    	return formatter.format(val);
    }
}
