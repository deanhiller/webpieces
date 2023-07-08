package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.metrics.Monitoring;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsRunnable extends MetricsSupplier<Void> {

    private static final Logger log = LoggerFactory.getLogger(MetricsRunnable.class);

    public MetricsRunnable(Monitoring monitoring, Supplier<Void> function, Map<String, String> tags, boolean legacyMdcHack) {
        super(monitoring, function, null, tags, legacyMdcHack);
    }



}
