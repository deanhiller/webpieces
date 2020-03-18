package org.webpieces.util.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class MetricStrategy {

	//TODO: Allow plugging in to this later to modify...
	public static String formName(String id) {
		return "webpieces/"+id;
	}


	public static void monitorExecutor(Executor executor, String id) {
		String metricName = formName(id);
		ExecutorServiceMetrics.monitor(Metrics.globalRegistry, executor, metricName);
	}
}
