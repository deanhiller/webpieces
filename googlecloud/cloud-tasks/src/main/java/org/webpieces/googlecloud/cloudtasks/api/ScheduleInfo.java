package org.webpieces.googlecloud.cloudtasks.api;

import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.Task;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ScheduleInfo {

    private Long taskTimeoutSeconds;
    private boolean scheduledInFuture = false;
    private long time;
    private Function<HttpRequest, Task> taskCreator;

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

    public Function<HttpRequest, Task> getTaskCreator() {
        return taskCreator;
    }

    public void setTaskCreator(Function<HttpRequest, Task> taskCreator) {
        this.taskCreator = taskCreator;
    }
}
