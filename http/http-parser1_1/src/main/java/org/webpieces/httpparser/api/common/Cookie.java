package org.webpieces.httpparser.api.common;

/**
 * See http://www.owasp.org/index.php/HttpOnly
 */
public class Cookie {

	private String name;
    private String value;
    
	private String path;
	private String domain;
	
	private boolean isSecure;
    private Integer maxAgeSeconds;
    
    private boolean isHttpOnly = true;
    
    /**
     * From https://www.owasp.org/index.php/HttpOnly
     * 
     * Set-Cookie: <name>=<value>[; <Max-Age>=<age>]
     * [; expires=<date>][; domain=<domain_name>]
     * [; path=<some_path>][; secure][; HttpOnly]
     * 
     * and http://mrcoles.com/blog/cookies-max-age-vs-expires/
     * 
     * @param cookie
     * @return
     */
    public Header createHeader() {
    	String headerVal = "";
    	if(name != null)
    		headerVal = name +"=";
    	else if(value == null)
    		throw new IllegalArgumentException("value in cookie cannot be null");
    	headerVal += value;
    	
    	if(maxAgeSeconds != null)
    		headerVal += "; Max-Age="+maxAgeSeconds;
    	if(domain != null)
    		headerVal += "; domain="+domain;
    	if(path != null)
    		headerVal += "; path="+path;
    	if(isSecure)
    		headerVal += "; secure";
    	if(isHttpOnly)
    		headerVal += "; HttpOnly";

    	return new Header(KnownHeaderName.SET_COOKIE, headerVal);
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
    
}
