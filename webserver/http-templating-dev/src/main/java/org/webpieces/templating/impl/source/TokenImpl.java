package org.webpieces.templating.impl.source;

import org.webpieces.templating.api.Token;

public class TokenImpl implements Token {

	public int begin;
	public int end;
	public TemplateToken state;
	public int beginLineNumber;
	public int endLineNumber;
	public String source;
	private String filePath;

	public TokenImpl(String filePath, int begin, int end, TemplateToken state, int beginLineNumber, int endLineNumber, String source) {
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

	public String getCleanValue() {
		return getValue().replaceAll("\r", "").replaceAll("\n", " ").trim();
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getSourceLocation() {
		return "File="+filePath+" line number="+beginLineNumber;
	}
}
