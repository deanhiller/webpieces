package org.webpieces.microsvc.monitoring.impl;

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

import org.webpieces.microsvc.monitoring.api.WebpiecesMetric;
import org.webpieces.microsvc.monitoring.api.WebpiecesMonitoring;

public class MicrometerMonitoring implements WebpiecesMonitoring {

    private static final Logger log = LoggerFactory.getLogger(MicrometerMonitoring.class);

    private final MeterRegistry registry;

    @Inject
    public MicrometerMonitoring(MeterRegistry registry) {
        this.registry = registry;
    }


    @Override
    public void duration(WebpiecesMetric metric, Map<String, String> dimensions, Duration duration) {
        List<Tag> tags = convertToTags(metric, dimensions);
        Timer timer = registry.timer(metric.getName(), tags);
        timer.record(duration);
    }

    @Override
    public void increment(WebpiecesMetric metric, Map<String, String> dimensions, double increment) {
        List<Tag> tags = convertToTags(metric, dimensions);
        Counter counter = registry.counter(metric.getName(), tags);
        counter.increment(increment);
    }

    private List<Tag> convertToTags(WebpiecesMetric metric, Map<String, String> dimensions) {

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
