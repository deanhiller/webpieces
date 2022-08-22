package org.webpieces.microsvc.monitoring.impl;

import java.time.Duration;
import java.util.Map;

import org.webpieces.microsvc.monitoring.api.WebpiecesMetric;
import org.webpieces.microsvc.monitoring.api.WebpiecesMonitoring;

public class NullMonitoring implements WebpiecesMonitoring {

    @Override
    public void duration(WebpiecesMetric metric, Map<String, String> dimensions, Duration duration) {

    }

    @Override
    public void increment(WebpiecesMetric metric, Map<String, String> dimensions, double increment) {

    }

}
