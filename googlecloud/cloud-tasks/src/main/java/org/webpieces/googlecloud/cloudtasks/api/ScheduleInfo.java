package org.webpieces.googlecloud.cloudtasks.api;

import java.util.concurrent.TimeUnit;

public class ScheduleInfo {
    private boolean scheduledInFuture = false;
    private long time;
    private TimeUnit timeUnit;

    public ScheduleInfo(long time, TimeUnit timeUnit) {
        this.scheduledInFuture = true;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public ScheduleInfo() {
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public boolean isScheduledInFuture() {
        return scheduledInFuture;
    }
}
