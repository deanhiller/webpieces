package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Object multiSelected = args.get("multiselected");
        if(body == null)
        	throw new IllegalArgumentException("Only #{option}#Some text#{/option}# can be used.  You cannot do #{option/}# "+srcLocation);
        if(multiSelected != null && selected != null)
        	throw new IllegalArgumentException("#{option}# tag can only have one of the multiselected or selected attributes not both. "+srcLocation);
        	
        
        String valueAsString = converters.convert(value);

        String selectedString = null;
        if(multiSelected != null) {
        	selectedString = detectMultiSelection(srcLocation, multiSelected, valueAsString);
        } else
        	selectedString = detectSingleSelection(srcLocation, selected, value, valueAsString);

		String bodyStr = ClosureUtil.toString(getName(), body, null);

        out.println("<option value=\""+valueAsString+"\"" + TemplateUtil.serialize(args, "value", "selected", "multiselected") + " "+selectedString+">"+bodyStr+"</script>");
	}

	private String detectMultiSelection(String srcLocation, Object multiSelected, String valueAsString) {
		if(!(multiSelected instanceof String))
			throw new IllegalArgumentException("multiselected attribute MUST be a String and was not.  it was of type="+multiSelected.getClass().getSimpleName()+"  "+srcLocation);

		String multiSelectedStr = (String) multiSelected;
		String[] split = multiSelectedStr.split(",");
		List<String> asList = Arrays.asList(split);
		
		if(asList.contains(valueAsString))
			return "selected=\"selected\"";
		
		return "";
	}

	private String detectSingleSelection(String srcLocation, Object selected, Object value, String valueAsString) {
        if(selected == null) {
        	if(value == null)
            	return "selected=\"selected\"";
        } else if(!(selected instanceof String))
         	throw new IllegalArgumentException("selected attribute MUST be a String and was not.  it was of type="+selected.getClass().getSimpleName()+"  "+srcLocation);
        
        else if(selected.equals(valueAsString))
        	return "selected=\"selected\"";
		return "";
	}

	@Override
	public String getName() {
		return "option";
	}
}
