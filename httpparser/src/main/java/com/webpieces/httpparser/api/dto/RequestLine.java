package com.webpieces.httpparser.api.dto;

public class RequestLine {
	private HttpUri uri;
	private HttpMethod method;
	private HttpVersion version = new HttpVersion();
	
	public HttpUri getUri() {
		return uri;
	}

	public void setUri(HttpUri httpUri) {
		this.uri = httpUri;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpVersion getVersion() {
		return version;
	}

	public void setVersion(HttpVersion version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		RequestLine other = (RequestLine) obj;
		if (method != other.method)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return method + " " +  uri + " " + version + "\r\n";
	}
}
