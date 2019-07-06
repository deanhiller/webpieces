package org.webpieces.webserver.api;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.function.Supplier;

import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.handlers.ConsumerFunc;

/**
 * EVERY server has a few modes.  The only exception is the http server will not let you set the sslEngineFactory
 * or it will throw an exception since it doesn't do SSL.
 * 
 * Mode 1: listenAddress=null sslEngineFactory=null - server disabled
 * Mode 2: listenAddress=null sslEngineFactory=YourSSLEngineFactory - server disabled
 * Mode 3: listenAddress={some port} sslEngineFactory=null - server will be started and will serve http.  This means
 *         for the https server, it WILL serve http and your firewall can hit port 443 without the need of the
 *         x-forwarded-proto.  The backend server will also serve http in this mode over it's port
 * Mode 4: listenAddress={some port} sslEngineFactory=YourSSLEngineFactory - server will be started and will serve
 *         https.  If you try this on the http server, it will throw an exception.
 *         
 * Some notes though.  You can terminate SSL at the firewall, and route to your http port for https as long as
 * you set the x-forwarded-proto to https.  This way, https pages are served through your http port BUT only for
 * those requests that terminated ssl on your firewall.  (not a good idea though if you are transferring credit
 * card information so I typically don't do that).
 * 
 * The backend server is a bit special.  If you disable it by not having the listenAddress set, those pages will
 * be served over the https server.  If you disable both https and http, the only way you can access https and
 * backend pages is by setting the x-forwarded-proto to https.
 * 
 * Because webpieces allows you to load certificates from a database, we like the idea of just terminating SSL 
 * on the webpieces server itself.  All servers in your cluster can load the one certificate in the database
 * and you can change the certificate in the database to get all servers to update.
 * 
 * @author dhiller
 *
 */
public class HttpSvrInstanceConfig {

	private Supplier<InetSocketAddress> listenAddress;
	private SSLEngineFactory sslEngineFactory;
	public ConsumerFunc<ServerSocketChannel> functionToConfigureBeforeBind;

	public HttpSvrInstanceConfig(Supplier<InetSocketAddress> httpAddr, SSLEngineFactory sslEngineFactory) {
		this.listenAddress = httpAddr;
		this.sslEngineFactory = sslEngineFactory;
	}
	
	public HttpSvrInstanceConfig() {
	}

	public Supplier<InetSocketAddress> getListenAddress() {
		return listenAddress;
	}
	
	public HttpSvrInstanceConfig setListenAddress(Supplier<InetSocketAddress> backendListenAddress) {
		this.listenAddress = backendListenAddress;
		return this;
	}

	public SSLEngineFactory getSslEngineFactory() {
		return sslEngineFactory;
	}
	
	public HttpSvrInstanceConfig setSslEngineFactory(SSLEngineFactory backendSslEngineFactory) {
		this.sslEngineFactory = backendSslEngineFactory;
		return this;
	}
	
	public ConsumerFunc<ServerSocketChannel> getFunctionToConfigureServerSocket() {
		return functionToConfigureBeforeBind;
	}

	public HttpSvrInstanceConfig setFunctionToConfigureServerSocket(ConsumerFunc<ServerSocketChannel> functionToConfigureServerSocket) {
		this.functionToConfigureBeforeBind = functionToConfigureServerSocket;
		return this;
	}
}
