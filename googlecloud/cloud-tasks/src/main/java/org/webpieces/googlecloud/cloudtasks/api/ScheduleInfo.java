package org.webpieces.googlecloud.cloudtasks.api;

import java.util.concurrent.TimeUnit;

public class ScheduleInfo {

    private Long taskTimeoutSeconds;
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

    public Long getTaskTimeoutSeconds() {
        return taskTimeoutSeconds;
    }

    public void setTaskTimeoutSeconds(Long taskTimeoutSeconds) {
        this.taskTimeoutSeconds = taskTimeoutSeconds;
    }
}
