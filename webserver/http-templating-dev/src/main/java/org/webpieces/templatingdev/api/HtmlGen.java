package org.webpieces.templatingdev.api;

public interface HtmlGen {

	String getName();
	
	void generateStartAndEnd(ScriptOutput sourceCode, Token token, int uniqueId);

	void generateStart(ScriptOutput sourceCode, Token token, int uniqueId);

	void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId);

}
