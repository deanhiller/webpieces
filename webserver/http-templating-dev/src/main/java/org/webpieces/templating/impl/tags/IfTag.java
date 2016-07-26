package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Tag;
import org.webpieces.templating.api.Token;

public class IfTag implements Tag {

	@Override
	public String getName() {
		return "if";
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token) {
		throw new IllegalArgumentException("if tag can only be used with a body so"
				+ " #{if/} is not usable.  location="+token.getSourceLocation());
	}
	
	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		String cleanValue = token.getCleanValue();
		int indexOf = cleanValue.indexOf(" ");
		if(indexOf < 0)
			throw new IllegalArgumentException("if statement is missing expression.  "
					+ "It must be #{if expression}# to work.  location="+token.getSourceLocation());
		String expression = cleanValue.substring(indexOf+1);
		sourceCode.println("if ("+expression+") {");
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		sourceCode.println("}");
	}

}
