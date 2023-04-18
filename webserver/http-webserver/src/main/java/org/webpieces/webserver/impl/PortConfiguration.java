package org.webpieces.webserver.impl;

import org.webpieces.util.cmdline2.Arguments;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

@Singleton
public class PortConfiguration {

	public static final String HTTP_PORT_KEY = "http.port";
	public static final String HTTPS_PORT_KEY = "https.port";
	public static final String HTTPS_OVER_HTTP = "https.over.http";

	//3 pieces consume this key to make it work :( and all 3 pieces do NOT depend on each other so
	//this key is copied in 3 locations
	public static final String BACKEND_PORT_KEY = "backend.port";

	private Supplier<InetSocketAddress> httpAddr;
	private Supplier<InetSocketAddress> httpsAddr;
	private Supplier<InetSocketAddress> backendAddr;
	private Supplier<Boolean> allowHttpsIntoHttp;

	public void runArguments(Arguments args) {
		//this is too late, have to do in the Guice modules
		httpAddr = args.createOptionalInetArg(HTTP_PORT_KEY, ":8080", "Http host&port.  syntax: {host}:{port} or just :{port} to bind to all NIC ips on that host");
		allowHttpsIntoHttp = args.createOptionalArg(HTTPS_OVER_HTTP, "false", "This enables the http port to receive SSL connections.", (s) -> Boolean.parseBoolean(s));
		httpsAddr = args.createOptionalInetArg(HTTPS_PORT_KEY, ":8443", "Http host&port.  syntax: {host}:{port} or just :{port} to bind to all NIC ips on that host");
		backendAddr = args.createOptionalInetArg(BACKEND_PORT_KEY, null, "Http(s) host&port for backend.  syntax: {host}:{port} or just :{port}.  Also, null means put the pages on the https/http ports");
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
