package org.webpieces.asyncserver.api;

import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.webpieces.nio.api.handlers.ConsumerFunc;

public class AsyncConfig {

	public String id;
	public SocketAddress bindAddr;
	public ConsumerFunc<ServerSocketChannel> functionToConfigureBeforeBind;
	
	public AsyncConfig(String id, SocketAddress bindAddr) {
		this.id = id;
		this.bindAddr = bindAddr;
	}

	public AsyncConfig() {
	}
	
}
