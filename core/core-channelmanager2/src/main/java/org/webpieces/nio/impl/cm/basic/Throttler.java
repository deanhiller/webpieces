package org.webpieces.nio.impl.cm.basic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.MaxRequestConfig;
import org.webpieces.nio.api.Throttle;

import javax.inject.Inject;

public class Throttler implements Throttle {
    public static final Logger log = Throttle.log;
    
    private final MaxRequestConfig maxRequestConfig;
    private final MeterRegistry metrics;
    private final Counter incrementCounter;
    private final Counter decrementCounter;

    private int outstandingRequests = 0;

    private boolean isThrottling;
    private Runnable turnThrottlingOff;

    public Throttler(BackpressureConfig backpressureConfig, MeterRegistry metrics) {
        this.maxRequestConfig = backpressureConfig.getMaxRequestConfig();
        this.metrics = metrics;
        incrementCounter = metrics.counter("webpieces.requests", "name", backpressureConfig.getName());
        decrementCounter = metrics.counter("webpieces.responses", "name", backpressureConfig.getName());
        metrics.gauge("webpieces.requests.inflight", outstandingRequests, (val) -> getValue(val));
    }

    private synchronized double getValue(Integer count) {
        //ignore and just return the current value inside synchronized block
        return outstandingRequests;
    }

    public synchronized void increment() {
        outstandingRequests++;
        incrementCounter.increment();
        //record metrics here still

        if(log.isDebugEnabled()) {
            if (outstandingRequests % 10 == 0)
                log.debug("throttler value=" + outstandingRequests);
        }

        if(maxRequestConfig == null)
            return;


        if(outstandingRequests > maxRequestConfig.getStartThrottlingAtRequestCount()) {
            if(!isThrottling)
                log.info("NOW THROTTLING requests. count="+outstandingRequests);
            isThrottling = true;
        }
    }

    public synchronized void decrement() {
        outstandingRequests--;
        decrementCounter.increment();

        if(outstandingRequests < 0 && outstandingRequests % 10 == 0)
            log.error("Bug, cannot go less than 0.  count="+outstandingRequests);

        if(log.isDebugEnabled()) {
            if (outstandingRequests % 10 == 0)
                log.debug("throttler value=" + outstandingRequests);
        }

        if(maxRequestConfig == null)
            return;

        if(outstandingRequests < maxRequestConfig.getStopThrottlingatRequestCount()) {
            if(isThrottling)
                log.info("TURNING OFF THROTTLING requests. count="+outstandingRequests);
            isThrottling = false;

            if(turnThrottlingOff != null) {
                turnThrottlingOff.run();
            } else {
                log.warn("FAIL, could not turn off throttling as no function installed - perhaps you are in embeeded test mode");
            }
        }
    }

    public boolean isThrottling() {
        return isThrottling;
    }

    @Override
    public void setFunctionToInvoke(Runnable turnThrottlingOff) {
        this.turnThrottlingOff = turnThrottlingOff;
    }


}
