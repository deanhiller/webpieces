package org.webpieces.googlecloud.logging;

public class GCPCloudLoggingTimestamp {

    private long seconds;
    private int nanos;

    public GCPCloudLoggingTimestamp() {}

    public GCPCloudLoggingTimestamp(long seconds, int nanos) {
        super();
        this.seconds = seconds;
        this.nanos = nanos;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public int getNanos() {
        return nanos;
    }

    public void setNanos(int nanos) {
        this.nanos = nanos;
    }


}
