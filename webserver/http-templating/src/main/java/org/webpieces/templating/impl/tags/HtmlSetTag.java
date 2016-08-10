package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class HtmlSetTag implements HtmlTag {

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        // Body case.  Users should not be using _ as prefix to variable names so _arg only exists if it is just a body
        Object name = args.get("_arg");
        if (name != null && body != null) {
        	String value = ClosureUtil.toString(body);
            template.putSetTagProperty(name, value);
            return;
        }
        
        // Simple case : #{set title:'Yop' /}
        for (Map.Entry<?, ?> entry : args.entrySet()) {
            Object key = entry.getKey();
            if (!key.toString().equals("arg")) {
            	Object val = entry.getValue();
            	if(val != null && val instanceof String) {
            		val = template.useFormatter(val);
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
