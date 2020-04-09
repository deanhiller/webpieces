package webpiecesxxxxxpackage;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MetricsModule implements Module {

	public MetricsModule(String instanceId) {
	}

	@Override
	public void configure(Binder binder) {
		CompositeMeterRegistry metrics = new CompositeMeterRegistry();
		metrics.add(new SimpleMeterRegistry());
		//Add Amazon or google or other here.  This one is google's...
		//metrics.add(StackdriverMeterRegistry.builder(stackdriverConfig).build());
		
		binder.bind(MeterRegistry.class).toInstance(metrics);
	}

}
