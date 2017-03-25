package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class AHrefTag implements HtmlTag {

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object url = args.get("href");
        if(url == null)
        	throw new IllegalArgumentException("#{a/}# tag must contain an href argument name like #{a href:@ROUTE[:]}#. "+srcLocation);
        else if(body == null)
        	throw new IllegalArgumentException("You must have an open and close tag(ie.  #{a}# and #{/a}#).  #{a/}# is not allowed"+srcLocation);

        out.print("<a href=\"" + url + "\"" + TemplateUtil.serialize(args, "href") + ">");
        out.print(ClosureUtil.toString(body));
        out.print("</a>");
	}

	@Override
	public String getName() {
		return "a";
	}
}
