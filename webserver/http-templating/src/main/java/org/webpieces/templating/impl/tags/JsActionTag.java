package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class JsActionTag implements HtmlTag {

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object url = args.get("defaultArgument");
        if(url == null)
        	throw new IllegalArgumentException("#{jsAction/}# tag must contain an default argument like #{jsAction @ROUTE[:]@}#. "+srcLocation);

        String html = "";
        html += "function(options) {\n";
        html += "  var pattern = '" + url.toString().replace("&amp;", "&") + "';\n";
        html += "  for(key in options) {\n";
        html += "    var val = options[key];\n";
        html += "    var nextVal = (val===undefined || val===null) ? '' : val\n";
        
        //NOTE: URL is encoded like so /ajax/user/confirmdelete/%7Bid%7D where %7B=={ and %7D==}
        html += "    pattern = pattern.replace('%7B'+key+'%7D', nextVal);\n";
        html += "  }\n";
        html += "  return pattern;\n";
        html += "}\n";
        out.println(html);
	}

	@Override
	public String getName() {
		return "jsAction";
	}
}
