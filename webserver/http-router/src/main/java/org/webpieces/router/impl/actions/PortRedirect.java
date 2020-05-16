package org.webpieces.router.impl.actions;

import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.routes.RouteId;

public class PortRedirect implements Redirect {

	private HttpPort port;
	private RouteId id;
	private Object[] args;

	public PortRedirect(HttpPort port, RouteId id, Object ... args) {
		this.port = port;
		this.id = id;
		this.args = args;
	}

	public RouteId getId() {
		return id;
	}

	public Object[] getArgs() {
		return args;
	}

	public HttpPort getPort() {
		return port;
	}

}
