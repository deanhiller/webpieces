package org.webpieces.webserver.test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLEngine;

import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.webserver.test.http11.DirectHttp11Client;

//A proxy to the real server for testing purposes
public class Http11ClientSimulator {

	//This is the direct client sitting on the server so you can step into the server AND it can keep the 
	//tests single threaded as well
	private DirectHttp11Client client;
	
	public Http11ClientSimulator(MockChannelManager mgr) {
		client = new DirectHttp11Client(mgr);
	}
	
	public Http11Socket createHttpSocket(InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		HttpSocket socket = client.createHttpSocket();
		CompletableFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return new Http11Socket(socket);
	}

	public Http11Socket createHttpsSocket(SSLEngine engine, InetSocketAddress addr) throws InterruptedException, ExecutionException, TimeoutException {
		HttpSocket socket = client.createHttpsSocket(engine);
		CompletableFuture<Void> connect = socket.connect(addr);
		connect.get(2, TimeUnit.SECONDS);
		return new Http11Socket(socket);
	}
}
