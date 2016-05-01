package com.webpieces.httpparser.api.dto;

public class HttpUri {

	private String uri;

	public HttpUri(String uri) {
	    if(uri == null || uri.length() == 0)
	        throw new IllegalStateException("url is not valid");
		this.uri = uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}

	public UrlInfo getHostPortAndType() {
	    int doubleslashIndex = uri.indexOf("://");
	    if(doubleslashIndex == -1)
	    	throw new UnsupportedOperationException("not supported yet");
	    
	    int domainStartIndex = doubleslashIndex+3;
	    String prefix = uri.substring(0, doubleslashIndex);
	    Integer port  = null;
	    
	    int firstSlashIndex = uri.indexOf('/', domainStartIndex);
	    if(firstSlashIndex < 0)
	    	firstSlashIndex = uri.length();

	    int domainEndIndex = firstSlashIndex;
	    int portIndex = uri.indexOf(':', domainStartIndex);
	    if(portIndex > 0 && portIndex < firstSlashIndex) {
	    	domainEndIndex = portIndex;
	    	String portStr = uri.substring(portIndex, firstSlashIndex);
	    	port = convert(portStr, uri);
	    }
	    	
	    String host = uri.substring(domainStartIndex, domainEndIndex);

	    return new UrlInfo(prefix, host, port);	    
	    
	}
	
	private Integer convert(String portStr, String uri2) {
		try {
			return Integer.parseInt(portStr);
		} catch(NumberFormatException e) {
			throw new IllegalStateException("port in uri="+uri2+" is not an integer", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		HttpUri other = (HttpUri) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "" + uri;
	}
	
}
