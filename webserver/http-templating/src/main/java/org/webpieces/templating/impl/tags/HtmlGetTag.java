package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class HtmlGetTag implements HtmlTag {

	@Override
	public void runTag(Map<?, ?> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object name = args.get("_arg");
        if(name == null)
        	name = args.get("key");
        if (name == null) {
        	//TODO: Figure out how to pass html line numbers and file name to tags for better reporting so we must
        	//generate source code info in the groovy scriptlet to do this...
            throw new IllegalArgumentException("#{get}# tag needs single argument or an argument named 'key'"+srcLocation);
        }
        
        boolean isFailFast = true;
        Object failFast = args.get("failfast");
        if(failFast != null && failFast instanceof Boolean) {
        	isFailFast = (Boolean)failFast;
        }
        
        Object result = template.getTemplateProperty(name);
        if(result != null) {
        	out.print(result);
        } else if(isFailFast) {
        	throw new IllegalArgumentException("#{get "+name+"/}# failed since property='"+name+"' was not found.  Perhaps you\n"
        			+ "forgot quotes around your argument? as it is then used as a variable if there are no quotes.\n"
        			+ "Another thing you could do is have the tag be #{get key:variable, failfast:false}# or #{get key:'name', failfast:false}# if\n"
        			+ "you would like it to not fail and just print whatever the body of the get is when the property is not found.\nlocation="+srcLocation);
        } else if(body != null){
        	//put the body of the #{get}#...body...#{/get}# into the page
        	String value = ClosureUtil.toString(body);
        	out.print(value);
        }
	}

	@Override
	public String getName() {
		return "get";
	}

}
