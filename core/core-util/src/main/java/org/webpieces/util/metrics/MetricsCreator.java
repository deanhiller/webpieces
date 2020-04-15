package org.webpieces.util.metrics;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.ToDoubleFunction;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

/**
 * Use for consistency which is VERY important on metrics in some cloud providers to keep metric count low
 * @author dean
 *
 */
public class MetricsCreator {

	public static final String NAME_PREFIX = "webpieces";
	
	public static void monitor(MeterRegistry metrics, Executor executor, String id) {
	    ExecutorServiceMetrics.monitor(metrics, executor, NAME_PREFIX+"."+id);
	}

	public static DistributionSummary createSizeDistribution(MeterRegistry metrics, String name, String sslType, String direction) {
		return DistributionSummary
			    .builder(NAME_PREFIX+".packetsize")
			    .tag("name", name)
			    .tag("sslType", sslType)
			    .tag("direction", direction)
			    .distributionStatisticBufferLength(1)
				.distributionStatisticExpiry(Duration.ofMinutes(10))
			    .publishPercentiles(0.5, 0.99, 1)
			    .baseUnit("bytes") // optional (1)
			    .register(metrics);
	}

	public static <T> void createGauge(MeterRegistry metrics, String name, T obj, ToDoubleFunction<T> valueFunction) {
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(Tag.of("name", name));
		metrics.gauge(NAME_PREFIX+".guageSize", tags, obj, valueFunction);
	}

	public static Counter createCounter(MeterRegistry metrics, String name, String type, boolean errorType) {
		String errors = "false";
		if(errorType)
			errors = "true";
		return metrics.counter(NAME_PREFIX+".counter", "name", name, "type", type, "isError", errors);
	}
	
}
