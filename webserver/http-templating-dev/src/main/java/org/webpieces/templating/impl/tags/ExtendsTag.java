package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.AbstractTag;
import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

public class ExtendsTag extends AbstractTag {

	@Override
	public String getName() {
		return "extends";
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token) {
		String name = getName();
		String cleanValue = token.getCleanValue();
		int indexOf = cleanValue.indexOf(" ");
		if(indexOf < 0)
			throw new IllegalArgumentException("extends tag is missing filename.  "
					+ "It must be #{extends filename/}# to work.  location="+token.getSourceLocation());
		String expression = cleanValue.substring(indexOf+1);
		
		
	}
	
	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		throw new IllegalArgumentException("#{extends}# are not allowed to be used.  Can only use #{extends/}#.  location="+token.getSourceLocation());

//		String cleanValue = token.getCleanValue();
//		int indexOf = cleanValue.indexOf(" ");
//		if(indexOf < 0)
//			throw new IllegalArgumentException("elseif statement is missing expression.  "
//					+ "It must be #{elseif expression}# to work.  location="+token.getSourceLocation());
//		String expression = cleanValue.substring(indexOf+1);
//		sourceCode.println(" else if ("+expression+") {");
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		throw new IllegalArgumentException("#{/extends}# are not allowed to be used.  Can only use #{extends/}#.  location="+token.getSourceLocation());
	}

}
