package org.webpieces.ctx.api;

public class ContentType {

	private String contentType;
	private String charSet;
	private String boundary;
	private String fullLine;

	public ContentType(String contentType, String charSet, String boundary, String fullLine) {
		this.contentType = contentType;
		this.charSet = charSet;
		this.boundary = boundary;
		this.fullLine = fullLine;
	}

	public String getContentType() {
		return contentType;
	}

	public String getCharSet() {
		return charSet;
	}

	public String getBoundary() {
		return boundary;
	}

	public String getFullLine() {
		return fullLine;
	}

	
}
