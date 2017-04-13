package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Map;

import org.webpieces.templating.api.ClosureUtil;
import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;

import groovy.lang.Closure;

public class OptionTag implements HtmlTag {

	private ConverterLookup converters;

	public OptionTag(ConverterLookup converters) {
		this.converters = converters;
	}
	
	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object selected = args.get("selected");
        Object value = args.get("value");
        if(body == null)
        	throw new IllegalArgumentException("Only #{option}#Some text#{/option}# can be used.  You cannot do #{option/}# "+srcLocation);

        String valueAsString = converters.convert(value);
        
        String selectedString = "";
        if(selected == null) {
        	if(value == null)
            	selectedString = "selected=\"selected\"";
        } else if(!(selected instanceof String))
         	throw new IllegalArgumentException("selected attribute MUST be a String and was not.  it was of type="+selected.getClass().getSimpleName()+"  "+srcLocation);
        else if(selected.equals(valueAsString))
        	selectedString = "selected=\"selected\"";

		String bodyStr = ClosureUtil.toString(getName(), body, null);

        out.println("<option value=\""+valueAsString+"\"" + TemplateUtil.serialize(args, "value", "selected") + " "+selectedString+">"+bodyStr+"</script>");
	}

	@Override
	public String getName() {
		return "option";
	}
}
