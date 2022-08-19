package org.webpieces.microsvc.monitoring.api;

import java.time.Duration;
import java.util.Map;

public interface WebpiecesMonitoring {

    default void duration(WebpiecesMetric metric, Map<String, String> dimensions, long startMillis, long endMillis) {
        duration(metric, dimensions, Duration.ofMillis(endMillis - startMillis));
    }

    void duration(WebpiecesMetric metric, Map<String, String> dimensions, Duration duration);

    default void increment(WebpiecesMetric metric, Map<String, String> dimensions) {
        increment(metric, dimensions, 1.0);
    }

    void increment(WebpiecesMetric metric, Map<String, String> dimensions, double increment);

}
