package com.webyoso.httpparser.api;

public class HttpVersion {

	private int major;
	private int minor;
	
	
	
	@Override
	public String toString() {
		return "[HTTP-Version=HTTP/" + major + "." + minor + "]";
	}
}
