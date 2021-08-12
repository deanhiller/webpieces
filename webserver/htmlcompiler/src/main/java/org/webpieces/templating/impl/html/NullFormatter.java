package org.webpieces.templating.impl.html;

import org.webpieces.templating.impl.EscapeCharactersFormatter;

public class NullFormatter implements EscapeCharactersFormatter {

	@Override
	public String format(Object val) {
		if(val == null)
			return null;
		return val.toString();
	}

}
