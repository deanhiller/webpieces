package org.webpieces.templating.api;

public interface Tag {

	String getName();
	
	void generateStartAndEnd(ScriptOutput sourceCode, Token token);

	void generateStart(ScriptOutput sourceCode, Token token);

	void generateEnd(ScriptOutput sourceCode, Token token);

}
