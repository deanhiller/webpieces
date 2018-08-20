package org.webpieces.templatingdev.impl.tags;

import org.webpieces.templatingdev.api.AbstractTag;
import org.webpieces.templatingdev.api.ScriptOutput;
import org.webpieces.templatingdev.api.Token;

public class VerbatimGen extends AbstractTag {

	@Override
	public String getName() {
		return "htmlEscapingOff";
	}
	
	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.println("      installNullFormatter();", token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		sourceCode.println("      installHtmlFormatter();", token);
		sourceCode.println();
	}

}
