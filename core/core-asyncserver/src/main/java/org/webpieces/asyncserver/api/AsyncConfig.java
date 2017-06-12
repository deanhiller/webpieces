package org.webpieces.asyncserver.api;

import java.nio.channels.ServerSocketChannel;

import org.webpieces.nio.api.handlers.ConsumerFunc;

public class AsyncConfig {

	public String id = "async-server";
	public ConsumerFunc<ServerSocketChannel> functionToConfigureBeforeBind;
	
	public AsyncConfig(String id) {
		this.id = id;
	}

	public AsyncConfig() {
	}
	
}
