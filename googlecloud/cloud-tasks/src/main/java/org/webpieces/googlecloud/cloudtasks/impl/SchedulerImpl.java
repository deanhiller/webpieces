package org.webpieces.googlecloud.cloudtasks.impl;

import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.util.concurrent.TimeUnit;

public class SchedulerImpl implements Scheduler {
    @Override
    public JobReference schedule(Runnable runnable, int time, TimeUnit timeUnit) {
        ScheduleInfo info = new ScheduleInfo(time, timeUnit);
        Context.put("webpieces-scheduleInfo", info);

        try {
            runnable.run();

            JobReference reference = Context.get("webpieces-scheduleResponse");
            return reference;
        } finally {
            Context.remove("scheduleInfo");
        }
    }

    @Override
    public JobReference addToQueue(Runnable runnable) {
        ScheduleInfo info = new ScheduleInfo();
        Context.put("webpieces-scheduleInfo", info);

        try {
            runnable.run();

            JobReference reference = Context.get("webpieces-scheduleResponse");
            return reference;
        } finally {
            Context.remove("scheduleInfo");
        }
    }

    @Override
    public XFuture<Void> cancelJob(JobReference ref) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
