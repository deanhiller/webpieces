package org.webpieces.templating.impl.source;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.ScriptOutput;

public class ScriptOutputImpl implements ScriptOutput {

	private StringBuffer scriptSourceCode = new StringBuffer();
	private int currentLine = 1;
	private Map<Integer, Integer> scriptLineNumToHtmlLineNum = new HashMap<>();
	private String packageStr;
	private String className;

	public ScriptOutputImpl(String packageStr, String className) {
		this.packageStr = packageStr;
		this.className = className;
	}

	public void println(String text) {
		scriptSourceCode.append(text);
        println();
	}

	void println() {
		scriptSourceCode.append("\n");
        currentLine++;
	}

	public void print(String text) {
		scriptSourceCode.append(text);
	}

	public void appendTokenComment(TokenImpl token) {
		scriptSourceCode.append(" //htmlLine ").append(token.beginLineNumber).append(":").append(token.endLineNumber);
		//we could have stored column info in the Token as well for here!! to append to comment
        scriptLineNumToHtmlLineNum .put(currentLine, token.beginLineNumber);		
	}

	public String getScriptSourceCode() {
		return scriptSourceCode.toString();
	}

	public Map<Integer, Integer> getLineMapping() {
		return scriptLineNumToHtmlLineNum;
	}

	public int getLineNumber() {
		return currentLine;
	}

	public String getFullClassName() {
		if(packageStr != null)
			return packageStr+"."+className;
		return className;
	}
	
}
