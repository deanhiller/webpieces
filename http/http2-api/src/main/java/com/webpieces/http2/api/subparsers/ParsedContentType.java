package com.webpieces.http2.api.subparsers;

public class ParsedContentType {

	private String mimeType;
	private String charSet;
	private String boundary;
	private String fullValue;

	public ParsedContentType(String mimeType, String charSet, String boundary, String fullValue) {
		this.mimeType = mimeType;
		this.charSet = charSet;
		this.boundary = boundary;
		this.fullValue = fullValue;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getCharSet() {
		return charSet;
	}

	public String getBoundary() {
		return boundary;
	}

	public String getFullValue() {
		return fullValue;
	}
	
}
