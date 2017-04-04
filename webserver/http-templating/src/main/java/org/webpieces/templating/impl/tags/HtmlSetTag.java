package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;
import org.webpieces.templating.impl.html.HTML;

import groovy.lang.Closure;

public class HtmlSetTag implements HtmlTag {

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        // Body case.  Users should not be using _ as prefix to variable names so _arg only exists if it is just a body
        Object name = args.get("defaultArgument");
        Object isEscaped = args.get("escaped");
        Boolean escaped = null;
        if(isEscaped instanceof Boolean)
        	escaped = (Boolean) isEscaped;
        if (name != null && body != null) {
        	storeBody(body, template, name, escaped);
            return;
        }
        
        storeSimpleProperties(args, template, escaped);
	}

	private void storeBody(Closure<?> body, GroovyTemplateSuperclass template, Object name, Boolean escaped) {
		boolean isEscaped = false; // The default as MOST of the time this is used like when using the extends tag you always want it this way
		if(escaped != null)
			isEscaped = escaped;
		
		String value = ClosureUtil.toString(body);
		if(isEscaped && value != null)
			value = HTML.htmlEscape(value);
		template.putSetTagProperty(name, value);
	}

	private void storeSimpleProperties(Map<Object, Object> args, GroovyTemplateSuperclass template, Boolean escaped) {
		boolean isEscaped = true; // The default as MOST of the time this is used like when using the extends tag you always want it this way
		if(escaped != null)
			isEscaped = escaped;
		
		// Simple case : #{set title:'Yop' /}
        for (Map.Entry<?, ?> entry : args.entrySet()) {
            Object key = entry.getKey();
            if (!key.toString().equals("arg")) {
            	Object val = entry.getValue();
            	if(isEscaped && val != null) {
            		val = HTML.htmlEscape(val.toString());
            	}
            	
                template.putSetTagProperty(key, val);
            }
        }
	}

	@Override
	public String getName() {
		return "set";
	}

}
