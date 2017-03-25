package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class ExtendsTag implements HtmlTag {

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object name = args.get("defaultArgument");
        if(name == null)
        	throw new IllegalArgumentException("#{extends/}# tag must contain a template name like #{extends '../template.html'/}#. "+srcLocation);
        else if(body != null)
        	throw new IllegalArgumentException("Only #{extends/}# can be used.  You cannot do #{extends}# #{/extends} as the body is not used with this tag"+srcLocation);
        
        //set the supertemplate on the template for later use by the template(This is a special tag where the platform and tag work together)
        template.setSuperTemplateFilePath(name.toString());
	}

	@Override
	public String getName() {
		return "extends";
	}

}
