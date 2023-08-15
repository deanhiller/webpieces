package org.webpieces.nio.api;

public class MaxRequestConfig {

    /**
     * Set to null to turn off throttling.  (Use at your own risk as too many in-flight requests can cause
     * an OOM)
     */
    private int startThrottlingAtRequestCount = 60;

    /**
     * In general, to not 'jitter' on/off, allow the server to catch up with in-flight requests and turn back
     * on at a lower threshold.  This is only used if overallQueueThrottle is not null
     */
    private int stopThrottlingatRequestCount = 10;

    public MaxRequestConfig() {
    }

    public MaxRequestConfig(int min, int max) {
        this.stopThrottlingatRequestCount = min;
        this.startThrottlingAtRequestCount = max;
    }

    public int getStartThrottlingAtRequestCount() {
        return startThrottlingAtRequestCount;
    }

    public void setStartThrottlingAtRequestCount(int startThrottlingAtRequestCount) {
        this.startThrottlingAtRequestCount = startThrottlingAtRequestCount;
    }

    public int getStopThrottlingatRequestCount() {
        return stopThrottlingatRequestCount;
    }

    public void setStopThrottlingatRequestCount(int stopThrottlingatRequestCount) {
        this.stopThrottlingatRequestCount = stopThrottlingatRequestCount;
    }
}
