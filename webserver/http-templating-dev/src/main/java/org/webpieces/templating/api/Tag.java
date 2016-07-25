package org.webpieces.templating.api;

public interface Tag {

	void generateStartAndEnd(ScriptOutput sourceCode);

	String getName();

	void generateStart(ScriptOutput sourceCode);

	void generateEnd(ScriptOutput sourceCode);

}
