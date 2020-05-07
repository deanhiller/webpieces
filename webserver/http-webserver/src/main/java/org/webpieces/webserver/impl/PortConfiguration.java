package org.webpieces.webserver.impl;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class PortConfiguration {

	private Supplier<InetSocketAddress> httpAddr;
	private Supplier<InetSocketAddress> httpsAddr;
	private Supplier<InetSocketAddress> backendAddr;
	private Supplier<Boolean> allowHttpsIntoHttp;

	public PortConfiguration(Supplier<InetSocketAddress> httpAddr, Supplier<InetSocketAddress> httpsAddr, Supplier<InetSocketAddress> backendAddr, Supplier<Boolean> allowHttpsIntoHttp) {
		this.httpAddr = httpAddr;
		this.httpsAddr = httpsAddr;
		this.backendAddr = backendAddr;
		this.allowHttpsIntoHttp = allowHttpsIntoHttp;
	}

	public Supplier<InetSocketAddress> getHttpAddr() {
		return httpAddr;
	}

	public Supplier<InetSocketAddress> getHttpsAddr() {
		return httpsAddr;
	}

	public Supplier<InetSocketAddress> getBackendAddr() {
		return backendAddr;
	}

	public Supplier<Boolean> getAllowHttpsIntoHttp() {
		return allowHttpsIntoHttp;
	}
}
