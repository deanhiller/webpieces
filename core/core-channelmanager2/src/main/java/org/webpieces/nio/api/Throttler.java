package org.webpieces.nio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Throttler implements Throttle {
    private static final Logger log = LoggerFactory.getLogger(Throttler.class);

    /**
     * Set to null to turn off throttling.  (Use at your own risk as too many in-flight requests can cause
     * an OOM)
     */
    private int maxConcurrentRequestThrottle = 60;

    /**
     * In general, to not 'jitter' on/off, allow the server to catch up with in-flight requests and turn back
     * on at a lower threshold.  This is only used if overallQueueThrottle is not null
     */
    private int minRequestsTurnOffThrottle = 10;

    private int outstandingRequests = 0;

    private boolean isThrottling;
    private Runnable turnThrottlingOff;

    public Throttler() {
    }

    public Throttler(int maxConcurrentRequestThrottle, int minRequestsTurnOffThrottle) {
        this.maxConcurrentRequestThrottle = maxConcurrentRequestThrottle;
        this.minRequestsTurnOffThrottle = minRequestsTurnOffThrottle;
    }

    public synchronized void increment() {
        outstandingRequests++;

        if(log.isDebugEnabled()) {
            if (outstandingRequests % 10 == 0)
                log.debug("throttler value=" + outstandingRequests);
        }

        if(outstandingRequests > maxConcurrentRequestThrottle) {
            if(!isThrottling)
                log.info("NOW THROTTLING requests. count="+outstandingRequests);
            isThrottling = true;
        }
    }

    public synchronized void decrement() {
        outstandingRequests--;
        if(outstandingRequests < 0 && outstandingRequests % 10 == 0)
            log.error("Bug, cannot go less than 0.  count="+outstandingRequests);

        if(log.isDebugEnabled()) {
            if (outstandingRequests % 10 == 0)
                log.debug("throttler value=" + outstandingRequests);
        }

        if(outstandingRequests < minRequestsTurnOffThrottle) {
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
