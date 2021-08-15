package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class StyleSheetTag implements HtmlTag {

	private RouterLookup lookup;

	public StyleSheetTag(RouterLookup lookup) {
		this.lookup = lookup;
	}
	
	//    <link rel="stylesheet" href="/assets/crud/css/theme.css?hash=ehehehehe" type="text/css" />
	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object url = args.get("href");
        if(url == null)
        	throw new IllegalArgumentException("#{stylesheet/}# tag must contain an href argument name like #{stylesheet href:'/assets/crud/css/theme.css'}#. "+srcLocation);
        else if(body != null)
        	throw new IllegalArgumentException("Only #{stylesheet/}# can be used.  You cannot do #{stylesheet}# #{/stylesheet} as the body is not used with this tag"+srcLocation);

        String rel = "stylesheet";
        String type = "text/css";

        Object maybeRel = args.get("rel");
        if(maybeRel != null)
        	rel = maybeRel + "";
        
        Object maybeType = args.get("type");
        if(maybeType != null)
        	type = maybeType + "";

        String hash = lookup.pathToUrlEncodedHash(url+"");
        if(hash != null)
        	url = url + "?hash="+hash;
        
        out.println("<link rel=\""+rel+"\" type=\""+type+"\" href=\""+url+"\" " + TemplateUtil.serialize(args, "href") + "/>");
	}

	@Override
	public String getName() {
		return "stylesheet";
	}
}
