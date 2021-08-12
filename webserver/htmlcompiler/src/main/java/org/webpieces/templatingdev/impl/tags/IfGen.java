package org.webpieces.templatingdev.impl.tags;

import org.webpieces.templatingdev.api.AbstractGroovyGen;
import org.webpieces.templatingdev.api.ScriptOutput;
import org.webpieces.templatingdev.api.Token;

public class IfGen extends AbstractGroovyGen {

	@Override
	public String getName() {
		return "if";
	}
	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId) {
		String cleanValue = token.getCleanValue();
		int indexOf = cleanValue.indexOf(" ");
		if(indexOf < 0)
			throw new IllegalArgumentException("if statement is missing expression.  "
					+ "It must be #{if expression}# to work. "+token.getSourceLocation(true));
		String expression = cleanValue.substring(indexOf+1);
		sourceCode.println("if ("+expression+") {", token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.println("}", token);
		sourceCode.println();
	}

}
