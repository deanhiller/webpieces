package org.webpieces.asyncserver.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;

public class ConnectedChannels {

	private Set<Channel> connectedChannels = new HashSet<>();
	private boolean closed;
	
	public synchronized boolean addChannel(Channel channel) {
		if(closed) {
			channel.close();
			return false;
		}
		
		this.connectedChannels.add(channel);
		return true;
	}
	
	public synchronized void removeChannel(Channel channel) {
		this.connectedChannels.remove(channel);
	}

	public synchronized Set<Channel> getAllChannels() {
		Set<Channel> copy = new HashSet<>();
		for(Channel c : connectedChannels) {
			copy.add(c);
		}
		
		return copy;
	}

	public synchronized CompletableFuture<Void> closeChannels() {
		List<CompletableFuture<Channel>> futures = new ArrayList<>();
		for(Channel c : getAllChannels()) {
			futures.add(c.close());
		}
		
		@SuppressWarnings("rawtypes")
		CompletableFuture[] array = futures.toArray(new CompletableFuture[0]);
		closed = true;
		return CompletableFuture.allOf(array);
	}
	
}
