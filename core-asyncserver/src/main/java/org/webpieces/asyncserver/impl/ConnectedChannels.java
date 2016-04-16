package org.webpieces.asyncserver.impl;

import java.util.HashSet;
import java.util.Set;

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
	
}
