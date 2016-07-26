package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Tag;
import org.webpieces.templating.api.Token;

public class VerbatimTag implements Tag {

	@Override
	public String getName() {
		return "verbatim";
	}
	
	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token) {
		throw new IllegalArgumentException("verbatim tag can only be used with a body so #{verbatim/}# is not usable.  location="+token.getSourceLocation());
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token) {
		sourceCode.println("      installNullFormatter();");
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token) {
		sourceCode.println("      installHtmlFormatter();");
	}

}
