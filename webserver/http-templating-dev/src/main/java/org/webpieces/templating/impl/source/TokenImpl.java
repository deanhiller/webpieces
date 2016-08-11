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
		String value = source.substring(begin, end);
		return value;
	}

	public String getCleanValue() {
		return getValue().replaceAll("\r", "").replaceAll("\n", " ").trim();
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getSourceLocation(boolean dueToError) {
		String fileName = filePath;
		int lastIndexOf = filePath.lastIndexOf("/");
		if(lastIndexOf > 0) 
			fileName = filePath.substring(lastIndexOf+1);
		String filePathAsClassName = filePath.replace("/", ".");
		String loc = "at "+filePathAsClassName+"("+fileName+":"+ beginLineNumber+")";
		if(!dueToError)
			return loc;
		
		return "\n\t"+loc+"\n";
	}
	
	@Override
	public boolean isEndTag() {
		return state == TemplateToken.END_TAG;
	}
	
	public String getTagName() {
		String expr = getCleanValue();
		int indexOfSpace = expr.indexOf(" ");
		String tagName = expr;
		if(indexOfSpace > 0) {
			tagName = expr.substring(0, indexOfSpace);
		}
		return tagName;
	}
}
