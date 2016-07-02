package org.webpieces.asyncserver.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class ConnectedChannels {

	private Set<TCPChannel> connectedChannels = new HashSet<>();
	
	public synchronized void addChannel(TCPChannel channel) {
		this.connectedChannels.add(channel);
	}
	
	public synchronized void removeChannel(TCPChannel channel) {
		this.connectedChannels.remove(channel);
	}

	public synchronized Set<TCPChannel> getAllChannels() {
		Set<TCPChannel> copy = new HashSet<>();
		for(TCPChannel c : connectedChannels) {
			copy.add(c);
		}
		
		return copy;
	}

	public CompletableFuture<Void> closeChannels() {
		List<CompletableFuture<Channel>> futures = new ArrayList<>();
		for(TCPChannel c : getAllChannels()) {
			futures.add(c.close());
		}
		
		@SuppressWarnings("rawtypes")
		CompletableFuture[] array = futures.toArray(new CompletableFuture[0]);
		return CompletableFuture.allOf(array);
	}
	
}
