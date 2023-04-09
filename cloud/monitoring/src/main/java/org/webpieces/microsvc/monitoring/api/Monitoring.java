package org.webpieces.microsvc.monitoring.api;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.microsvc.monitoring.impl.MonitoringImpl;

@ImplementedBy(MonitoringImpl.class)
public interface Monitoring {

    void duration(Metric metric, Map<String, String> dimensions, long startMillis);

    void duration(Metric metric, Map<String, String> dimensions, Duration duration);

    void increment(Metric metric, Map<String, String> dimensions);

    void increment(Metric metric, Map<String, String> dimensions, double increment);
}
