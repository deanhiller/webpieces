package org.webpieces.googlecloud.cloudtasks.api;

import com.google.inject.ImplementedBy;
import org.webpieces.googlecloud.cloudtasks.impl.SchedulerImpl;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@ImplementedBy(SchedulerImpl.class)
public interface Scheduler {
    XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, long epochMsToRunAt);

    XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable);

    XFuture<Void> cancelJob(JobReference ref);
}
