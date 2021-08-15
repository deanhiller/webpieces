package org.webpieces.templating.impl.html;

import org.webpieces.templating.impl.EscapeCharactersFormatter;

public class EscapeHTMLFormatter implements EscapeCharactersFormatter {

    @Override
    public String format(Object value) {
    	if(value == null)
    		return "";
    	return HTML.htmlEscape(value.toString());
    }
}
