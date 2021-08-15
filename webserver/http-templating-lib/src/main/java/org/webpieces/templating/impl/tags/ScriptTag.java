package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class ScriptTag implements HtmlTag {

	private RouterLookup lookup;

	public ScriptTag(RouterLookup lookup) {
		this.lookup = lookup;
	}
	
	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object src = args.get("src");
        if(src == null)
        	throw new IllegalArgumentException("#{script/}# tag must contain an src argument name like #{script src:'/assets/crud/css/theme.js'}#. "+srcLocation);
        else if(body != null)
        	throw new IllegalArgumentException("Only #{script/}# can be used.  You cannot do #{script}# #{/script} as the body is not used with this tag and would not be useful anyways "+srcLocation);

        String hash = lookup.pathToUrlEncodedHash(src+"");
        if(hash != null)
        	src = src + "?hash="+hash;
        
        out.println("<script src=\""+src+"\" " + TemplateUtil.serialize(args, "src") + "></script>");
	}

	@Override
	public String getName() {
		return "script";
	}
}
