package org.webpieces.asyncserver.impl;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.metrics.MetricsCreator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class ConnectedChannels {

	/**
	 * This will leak as removeChannel is not guaranteed (for some reason :( ).
	 */
	private WeakHashMap<Channel, Boolean> connectedChannels = new WeakHashMap<>();

	private volatile boolean closed;
	private Counter addedCounter;
	private Counter removedCounter;
	
	public ConnectedChannels(String id, MeterRegistry metrics) {
		
		MetricsCreator.createGauge(metrics, id+".connectionCount", connectedChannels, (c) -> c.size());
		
		addedCounter = MetricsCreator.createCounter(metrics, id, "connectionsAdded", false);
		removedCounter = MetricsCreator.createCounter(metrics, id, "connectionsRemoved", false);
	}

	public synchronized boolean addChannel(Channel channel) {
		if(closed) {
			channel.close();
			return false;
		}
		
		addedCounter.increment();
		this.connectedChannels.put(channel, true);
		return true;
	}
	
	public synchronized void removeChannel(Channel channel) {
		if(closed) {
			return; //don't allow any threads to modify as closeChannels will be doing it
		}
		removedCounter.increment();
		this.connectedChannels.remove(channel);
	}

	public synchronized XFuture<Void> closeChannels() {
		//first prevent other threads from calling above functions ever again
		closed = true;

		List<XFuture<Void>> futures = new ArrayList<>();
		for(Channel c : connectedChannels.keySet()) {
			futures.add(c.close());
		}
		
		@SuppressWarnings("rawtypes")
		XFuture[] array = futures.toArray(new XFuture[0]);
		return XFuture.allOf(array);
	}
	
}
