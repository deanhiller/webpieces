package org.webpieces.httpparser.api.dto;

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
		String last = version.substring(index+1);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HttpVersion other = (HttpVersion) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HTTP/" + major + "." + minor;
	}
}
