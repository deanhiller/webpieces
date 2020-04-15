package org.webpieces.asyncserver.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.metrics.MetricsCreator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class ConnectedChannels {

	private ConcurrentHashMap<Channel, Boolean> connectedChannels = new ConcurrentHashMap<>();
	private volatile boolean closed;
	private Counter addedCounter;
	private Counter removedCounter;
	
	public ConnectedChannels(String id, MeterRegistry metrics) {
		
		MetricsCreator.createGauge(metrics, id+".connectionCount", connectedChannels, (c) -> c.size());
		
		addedCounter = MetricsCreator.createCounter(metrics, id, "connectionsAdded", false);
		removedCounter = MetricsCreator.createCounter(metrics, id, "connectionsRemoved", false);
	}

	public boolean addChannel(Channel channel) {
		if(closed) {
			channel.close();
			return false;
		}
		
		addedCounter.increment();
		this.connectedChannels.put(channel, true);
		return true;
	}
	
	public void removeChannel(Channel channel) {
		if(closed) {
			return; //don't allow any threads to modify as closeChannels will be doing it
		}
		removedCounter.increment();
		this.connectedChannels.remove(channel);
	}

	public CompletableFuture<Void> closeChannels() {
		//first prevent other threads from calling above functions ever again
		closed = true;

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for(Channel c : connectedChannels.keySet()) {
			futures.add(c.close());
		}
		
		@SuppressWarnings("rawtypes")
		CompletableFuture[] array = futures.toArray(new CompletableFuture[0]);
		return CompletableFuture.allOf(array);
	}
	
}
