package org.webpieces.templating.api;

public interface ScriptOutput {

	void print(String string);
	
	void println(String string, Token forLineNumberComment);

	void println();
	
}
