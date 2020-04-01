package WEBPIECESxPACKAGE;

import com.google.inject.Binder;
import com.google.inject.Module;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class SimpleMeterModule implements Module {

	private SimpleMeterRegistry metrics;

	public SimpleMeterModule(SimpleMeterRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(MeterRegistry.class).toInstance(metrics);
	}

}
