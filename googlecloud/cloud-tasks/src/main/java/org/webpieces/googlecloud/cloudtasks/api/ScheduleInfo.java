package org.webpieces.googlecloud.cloudtasks.api;

import java.util.concurrent.TimeUnit;

public class ScheduleInfo {
    private boolean scheduledInFuture = false;
    private int time;
    private TimeUnit timeUnit;

    public ScheduleInfo(int time, TimeUnit timeUnit) {
        this.scheduledInFuture = true;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public ScheduleInfo() {
    }

    public int getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public boolean isScheduledInFuture() {
        return scheduledInFuture;
    }
}
