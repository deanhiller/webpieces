package org.webpieces.util.acking;

import io.micrometer.core.instrument.MeterRegistry;

public class AckMetrics {

//	private Counter tracked;
//	private Counter acked;

	public AckMetrics(MeterRegistry metrics, String name) {
//		tracked = metrics.counter(name+".trackedbytes");
//		acked = metrics.counter(name+".ackedbytes");
	}

	public void incrementTrackedBytes(int incomingBytes) {
//		tracked.increment(incomingBytes);
	}

	public void incrementAckedBytes(int numBytes) {
//		acked.increment(numBytes);
	}
}
