package org.webpieces.templating.impl.source;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.templating.api.ScriptOutput;
import org.webpieces.templating.api.Token;

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

	@Override
    public void println(String text, Token forLineNumberComment) {
		scriptSourceCode.append(text);
		if(forLineNumberComment != null)
			appendTokenComment(forLineNumberComment);
		println();
	}

	@Override
    public void println() {
		scriptSourceCode.append("\n");
        currentLine++;
	}

	@Override
    public void print(String text) {
		scriptSourceCode.append(text);
	}

	private void appendTokenComment(Token t) {
		TokenImpl token = (TokenImpl) t;
		scriptSourceCode.append(" //htmlLine ").append(token.beginLineNumber).append(":").append(token.endLineNumber)
			.append("  groovyLine=").append(currentLine);
		//we could have stored column info in the Token as well for here!! to append to comment
        scriptLineNumToHtmlLineNum.put(currentLine, token.beginLineNumber);		
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

	@Override
	public String toString() {
		return "ScriptOutputImpl.scriptSourceCode=\n\n" + scriptSourceCode;
	}
	
}
