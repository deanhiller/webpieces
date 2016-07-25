package org.webpieces.templating.impl.source;

public class Token {

	public int begin;
	public int end;
	public ScriptToken state;
	public int beginLineNumber;
	public int endLineNumber;
	public String source;
	private String filePath;

	public Token(String filePath, int begin, int end, ScriptToken state, int beginLineNumber, int endLineNumber, String source) {
		this.filePath = filePath;
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

	public String getFilePath() {
		return filePath;
	}
}
