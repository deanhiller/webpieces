package org.webpieces.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

public class Monitoring {

    private static final Logger log = LoggerFactory.getLogger(Monitoring.class);

    private final MeterRegistry metrics;

    @Inject
    public Monitoring(MeterRegistry metrics) {
        this.metrics = metrics;
    }

    public void endTimer(String metric, Map<String, String> metricTags, long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        recordTimeMetric(metric, metricTags, duration);
    }

    public void recordTimeMetric(String metric, Map<String, String> metricTags, long duration) {
        List<Tag> tags = convertTagsMap(metricTags);
        Timer timer = metrics.timer(metric, tags);
        timer.record(duration, TimeUnit.MILLISECONDS);
    }

    public <T> void gauge(String metric, Map<String, String> metricTags, T obj, ToDoubleFunction<T> valueFunction) {
        gauge(metric, metricTags, valueFunction.applyAsDouble(obj));
    }

    public void gauge(String metric, Map<String, String> metricTags, Double value) {
        List<Tag> tags = convertTagsMap(metricTags);
        metrics.gauge(metric, tags, value);
    }

    public void incrementMetric(String metric, Map<String, String> metricTags) {
        incrementMetric(metric, metricTags, 1);
    }

    public void incrementMetric(String metric, Map<String, String> metricTags, double incrementBy) {
        List<Tag> tags = convertTagsMap(metricTags);
        Counter counter = metrics.counter(metric, tags);
        counter.increment(incrementBy);
    }

    private List<Tag> convertTagsMap(Map<String, String> tagsMap) {
        List<Tag> tags = new ArrayList<>();
        for (var entry : tagsMap.entrySet()) {
            tags.add(Tag.of(entry.getKey(), entry.getValue()));
        }
        return tags;
    }
}
