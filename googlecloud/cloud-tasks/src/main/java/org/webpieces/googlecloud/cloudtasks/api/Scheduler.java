package org.webpieces.googlecloud.cloudtasks.api;

import com.google.cloud.tasks.v2.HttpRequest;
import com.google.cloud.tasks.v2.Task;
import com.google.inject.ImplementedBy;
import org.webpieces.googlecloud.cloudtasks.impl.SchedulerImpl;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@ImplementedBy(SchedulerImpl.class)
public interface Scheduler {

    @Deprecated
    XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, long epochMsToRunAt);

    XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, long epochMsToRunAt, long taskTimeoutSeconds);

    XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, Function<HttpRequest, Task> creatorFunction);

    @Deprecated
    XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable);

    XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable, long taskTimeoutSeconds);

    XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable, Function<HttpRequest, Task> creatorFunction);

    XFuture<Void> cancelJob(JobReference ref);
}
