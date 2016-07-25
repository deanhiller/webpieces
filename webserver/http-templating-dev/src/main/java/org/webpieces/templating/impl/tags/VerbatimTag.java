package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Tag;
import org.webpieces.templating.impl.source.ScriptOutputImpl;

public class VerbatimTag implements Tag {

	@Override
	public String getName() {
		return "verbatim";
	}
	
	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode) {
		//do nothing
	}

	@Override
	public void generateStart(ScriptOutput sourceCode) {
		sourceCode.println("      installNullFormatter();");
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode) {
		sourceCode.println("      installHtmlFormatter();");
	}

}
