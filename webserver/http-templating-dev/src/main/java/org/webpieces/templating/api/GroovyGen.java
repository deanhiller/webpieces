package org.webpieces.templating.api;

public interface GroovyGen {

	String getName();
	
	void generateStartAndEnd(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback);

	void generateStart(ScriptOutput sourceCode, Token token, int uniqueId, CompileCallback callback);

	void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId);

}
