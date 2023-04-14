package org.webpieces.googlecloud.cloudtasks.api;

import java.util.concurrent.TimeUnit;

public class ScheduleInfo {
    private boolean scheduledInFuture = false;
    private long time;

    public ScheduleInfo(long epochMsToRun) {
        this.scheduledInFuture = true;
        this.time = epochMsToRun;
    }

    public ScheduleInfo() {
    }

    public long getTime() {
        return time;
    }

    public boolean isScheduledInFuture() {
        return scheduledInFuture;
    }
}
