package org.webpieces.router.impl.model;

public class RouterInfo {

	private String routerId;
	private String path;

	public RouterInfo(String routerId) {
		this(routerId, "");
	}
	
	public RouterInfo(String id, String path) {
		this.routerId = id;
		this.path = path;
	}

	public String getRouterId() {
		return routerId;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		//any host EXCEPT specific hosts that were defined by client app
		//ie. not any host, but we print xxxhost instead
		String host = "<xxxhost:anyContent>";
		if(routerId != null)
			host = routerId;
		return host+path;
	}
	
}
