package org.webpieces.googlecloud.cloudtasks.impl;

import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static org.webpieces.googlecloud.cloudtasks.impl.Constants.*;

public class SchedulerImpl implements Scheduler {

    @Override
    public XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, int time, TimeUnit timeUnit) {
        ScheduleInfo info = new ScheduleInfo(time, timeUnit);
        return executeIt(runnable, info);
    }

    @Override
    public XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable) {
        ScheduleInfo info = new ScheduleInfo();
        return executeIt(runnable, info);
    }

    private XFuture<JobReference> executeIt(Supplier<XFuture<Void>> runnable, ScheduleInfo info) {
        Context.put(WEBPIECES_SCHEDULE_INFO, info);
        Context.put(Constants.WEBPIECES_SCHEDULE_ACTION, RemoteInvokerAction.GCP_CREATE_TASK);
        XFuture<Void> future = runnable.get();
        return future.thenApply(v -> {
            JobReference reference = Context.get(Constants.WEBPIECES_SCHEDULE_RESPONSE);
            return reference;
        });
    }

    @Override
    public XFuture<Void> cancelJob(JobReference ref) {
        ScheduleInfo info = new ScheduleInfo();
        Context.put(WEBPIECES_SCHEDULE_INFO, info);
        Context.put(Constants.WEBPIECES_SCHEDULE_ACTION, RemoteInvokerAction.GCP_DELETE_TASK);
        Context.put(Constants.WEBPIECES_SCHEDULE_DELETE_TASK, ref.getTaskId());
        return new XFuture<>();
    }
}
