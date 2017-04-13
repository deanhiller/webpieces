package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class FormTag implements HtmlTag {

	private Charset defaultFormAcceptEncoding;

	public FormTag(Charset defaultFormAcceptEncoding) {
		this.defaultFormAcceptEncoding = defaultFormAcceptEncoding;
	}

	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object action = args.get("action");
        if(action == null)
        	throw new IllegalArgumentException("#{form/}# tag must have an action argument like #{form action:@ROUTE[:]}#. "+srcLocation);

        String url = action.toString();
                
        String method = "POST";
        if (args.containsKey("method"))
        	method = args.get("method").toString();
        
        String encoding = defaultFormAcceptEncoding.name(); 
        if(args.containsKey("accept-charset"))
        	encoding = args.get("accept-charset").toString();
        
        String enctype = (String) args.get("enctype");
        if (enctype == null)
            enctype = "application/x-www-form-urlencoded";
        
        String secureToken = Current.session().getSecureToken();
        String tokenKeyName = RequestContext.SECURE_TOKEN_FORM_NAME;
        
        String formHeader = "<form action=`" + url + "` method=`" + method.toLowerCase() + "` accept-charset=`" + encoding.toLowerCase()
        	+ "` enctype=`" + enctype + "`" + TemplateUtil.serialize(args, "action", "method", "accept-charset", "enctype") + ">";
        String secureInputElement = "    <input type=`hidden` name=`"+tokenKeyName+"` value=`"+secureToken+"`>";
        
        out.println(formHeader.replaceAll("`", "\""));
        out.println(secureInputElement.replaceAll("`", "\""));
        out.println(ClosureUtil.toString(getName(), body, null));
        out.print("</form>");
	}

	@Override
	public String getName() {
		return "form";
	}
}
