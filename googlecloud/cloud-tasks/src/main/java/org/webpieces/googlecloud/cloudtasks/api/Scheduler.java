package org.webpieces.googlecloud.cloudtasks.api;

import com.google.inject.ImplementedBy;
import org.webpieces.googlecloud.cloudtasks.impl.SchedulerImpl;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.TimeUnit;

@ImplementedBy(SchedulerImpl.class)
public interface Scheduler {
    JobReference schedule(Runnable runnable, int time, TimeUnit timeUnit);

    JobReference addToQueue(Runnable runnable);

    XFuture<Void> cancelJob(JobReference ref);
}
