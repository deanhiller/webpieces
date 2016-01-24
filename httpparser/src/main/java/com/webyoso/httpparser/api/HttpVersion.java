package com.webyoso.httpparser.api;

public class HttpVersion {

	private int major = 1;
	private int minor = 1;

	/**
	 * Set the http version such as 1.1 or 1.2.  It must 
	 * be {integer}.{integer} format per the RFC.
	 * @param version
	 */
	public void setVersion(String version) {
		int index = version.indexOf(".");
		if(index < 0) 
			throw new IllegalStateException("Missing the '.' in the version number.  ie. 1.1.");
		
		String first = version.substring(0, index);
		String last = version.substring(index);
		major = convertToInteger(first);
		minor = convertToInteger(last);
	}
	
	private int convertToInteger(String first) {
		try {
			return Integer.valueOf(first);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Must be of format {integer}.{integer} but wasn't", e);
		}
	}

	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public String getVersion() {
		return major + "." + minor;
	}
	
	@Override
	public String toString() {
		return "HTTP/" + major + "." + minor;
	}
}
