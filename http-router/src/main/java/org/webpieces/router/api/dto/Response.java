package org.webpieces.router.api.dto;

public class Response {

	public Boolean isHttps;
	public String domain;
	public String path;

	public Response(Boolean isHttps, String domain, String path) {
		this.isHttps = isHttps;
		this.domain = domain;
		this.path = path;
	}

	@Override
	public String toString() {
		return "Response [isHttps=" + isHttps + ", domain=" + domain + ", path=" + path + "]";
	}
	
}
