package org.webpieces.templating.impl;

import groovy.lang.Script;

public abstract class GroovyTemplateSuperclass extends Script {

    // Leave this field public to allow custom creation of TemplateExecutionException from different pkg
	private EscapeCharactersFormatter formatter;

    public void initialize(EscapeCharactersFormatter formatter) {
    	this.formatter = formatter;
    }
    
    public String __escapeTextCharacters(Object val) {
        return formatter.format(val);
    }

}
