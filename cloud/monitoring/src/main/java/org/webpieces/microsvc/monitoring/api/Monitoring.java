package org.webpieces.microsvc.monitoring.api;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitoring {

    private static final Logger log = LoggerFactory.getLogger(Monitoring.class);

    private final MeterRegistry registry;

    @Inject
    public Monitoring(MeterRegistry registry) {
        this.registry = registry;
    }


    public void duration(Metric metric, Map<String, String> dimensions, long startMillis, long endMillis) {
        duration(metric, dimensions, Duration.ofMillis(endMillis - startMillis));
    }

    public void duration(Metric metric, Map<String, String> dimensions, Duration duration) {
        List<Tag> tags = convertToTags(metric, dimensions);
        Timer timer = Timer.builder(metric.getName()).tags(tags).publishPercentileHistogram().register(registry);
        timer.record(duration);
    }

    public void increment(Metric metric, Map<String, String> dimensions) {
        increment(metric, dimensions, 1.0);
    }

    public void increment(Metric metric, Map<String, String> dimensions, double increment) {
        List<Tag> tags = convertToTags(metric, dimensions);
        Counter counter = registry.counter(metric.getName(), tags);
        counter.increment(increment);
    }

    private List<Tag> convertToTags(Metric metric, Map<String, String> dimensions) {

        List<Tag> tags = new ArrayList<>();

        for(Map.Entry<String,String> entry : dimensions.entrySet()) {

            if(metric.getDimensions().contains(entry.getKey())) {
                tags.add(Tag.of(entry.getKey(), entry.getValue()));
            } else {
                log.warn("Metric dimension '{}' is not part of the given Metric class: {}", entry.getKey(), metric.getClass().getName());
            }

        }

        return tags;

    }

}
