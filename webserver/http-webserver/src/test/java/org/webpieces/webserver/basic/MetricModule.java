package org.webpieces.webserver.basic;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricModule implements Module {

	private SimpleMeterRegistry metrics;

	public MetricModule(SimpleMeterRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(MeterRegistry.class).toInstance(metrics);
	}

}
