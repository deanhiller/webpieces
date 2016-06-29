package org.webpieces.templating.impl;

public class Token {

	public int begin;
	public int end;
	public ScriptToken state;
	public int beginLineNumber;
	public int endLineNumber;
	public String source;

	public Token(int begin, int end, ScriptToken state, int beginLineNumber, int endLineNumber, String source) {
		this.begin = begin;
		this.end = end;
		this.state = state;
		this.beginLineNumber = beginLineNumber;
		this.endLineNumber = endLineNumber;
		this.source = source;
	}

	@Override
	public String toString() {
		return "Token [state=" + state + ", beginLineNumber=" + beginLineNumber
				+ ", endLineNumber=" + endLineNumber + ", begin=" + begin + ", end=" + end + ", snippet='"+getValue()+"']";
	}

	public String getValue() {
		return source.substring(begin, end);
	}

}
