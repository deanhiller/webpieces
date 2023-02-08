package org.webpieces.googlecloud.cloudtasks.impl;

import java.util.concurrent.TimeUnit;

public class ScheduleInfo {
    private final int time;
    private final TimeUnit timeUnit;

    public ScheduleInfo(int time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public int getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
