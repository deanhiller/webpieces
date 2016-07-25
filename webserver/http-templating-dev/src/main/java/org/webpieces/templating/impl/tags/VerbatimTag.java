package org.webpieces.templating.impl.tags;

import org.webpieces.templating.api.Tag;
import org.webpieces.templating.impl.source.ScriptCode;

public class VerbatimTag implements Tag {

	@Override
	public String getName() {
		return "verbatim";
	}
	
	@Override
	public void generateStartAndEnd(ScriptCode sourceCode) {
		//do nothing
	}

	@Override
	public void generateStart(ScriptCode sourceCode) {
		sourceCode.println("      installNullFormatter();");
	}

	@Override
	public void generateEnd(ScriptCode sourceCode) {
		sourceCode.println("      installHtmlFormatter();");
	}

}
