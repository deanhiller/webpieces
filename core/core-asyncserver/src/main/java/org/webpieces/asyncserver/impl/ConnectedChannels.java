package org.webpieces.asyncserver.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.webpieces.nio.api.channels.Channel;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public class ConnectedChannels {

	private ConcurrentHashMap<Channel, Boolean> connectedChannels = new ConcurrentHashMap<>();
	private volatile boolean closed;
	private Counter addedCounter;
	private Counter removedCounter;
	
	public ConnectedChannels(String id, MeterRegistry metrics) {
		String metricName = id + "/connections/count";
		metrics.gauge(metricName, connectedChannels, (c) -> c.size());
		
		String metricName1 = id + "/connections/added";		
		addedCounter = metrics.counter(metricName1);
		
		String metricName2 = id + "/connections/removed";
		removedCounter = metrics.counter(metricName2);

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
