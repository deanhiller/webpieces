package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Tag;
import org.webpieces.templating.api.Token;

public class ElseTag implements Tag {

	@Override
	public String getName() {
		return "else";
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token) {
		throw new IllegalArgumentException("else tag can only be used with a body so"
				+ " #{else/} is not usable.  location="+token.getSourceLocation());
	}
	
	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		sourceCode.println(" else {");
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		sourceCode.println("}");
	}

}
