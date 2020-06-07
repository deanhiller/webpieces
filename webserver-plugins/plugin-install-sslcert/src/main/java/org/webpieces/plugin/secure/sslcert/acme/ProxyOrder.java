package org.webpieces.plugin.secure.sslcert.acme;

import java.util.List;

import org.shredzone.acme4j.Order;

public class ProxyOrder {

	private Order order;
	private List<ProxyAuthorization> auths;

	public ProxyOrder(Order order, List<ProxyAuthorization> auths) {
		this.order = order;
		this.auths = auths;
	}

	public List<ProxyAuthorization> getAuthorizations() {
		return auths;
	}

	public Order getOrder() {
		return order;
	}

}
