package com.webpieces.hpack.api.subparsers;

/**
 * See http://www.owasp.org/index.php/HttpOnly
 */
public class ResponseCookie {

	private String name;
	private String value;
	private String path;
	private String domain;
	
	private boolean isSecure;
    private Integer maxAgeSeconds;
    
    private boolean isHttpOnly = true;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public boolean isSecure() {
		return isSecure;
	}

	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}

	public Integer getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public void setMaxAgeSeconds(Integer maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	public boolean isHttpOnly() {
		return isHttpOnly;
	}

	public void setHttpOnly(boolean isHttpOnly) {
		this.isHttpOnly = isHttpOnly;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
