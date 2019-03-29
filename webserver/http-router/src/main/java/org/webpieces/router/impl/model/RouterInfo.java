package org.webpieces.router.impl.model;

public class RouterInfo {

	private String domain;
	private String path;

	public RouterInfo(String domain) {
		this(domain, "");
	}
	
	public RouterInfo(String domain, String path) {
		this.domain = domain;
		this.path = path;
	}

	public String getDomain() {
		return domain;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		//any host EXCEPT specific hosts that were defined by client app
		//ie. not any host, but we print xxxhost instead
		String host = "<xxxhost>";
		if(domain != null)
			host = domain;
		return host+path;
	}
	
}
