package org.webpieces.templating.api;

public interface ScriptOutput {

	void print(String string);
	
	void println(String string);

	void appendTokenComment(Token token);

	void println();
	
}
