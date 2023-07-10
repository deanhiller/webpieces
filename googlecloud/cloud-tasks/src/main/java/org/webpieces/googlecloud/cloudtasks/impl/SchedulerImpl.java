package org.webpieces.googlecloud.cloudtasks.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.googlecloud.cloudtasks.api.JobReference;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.googlecloud.cloudtasks.api.Scheduler;
import org.webpieces.recorder.impl.EndpointInfo;
import org.webpieces.recorder.impl.TestCaseRecorder;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static org.webpieces.googlecloud.cloudtasks.impl.Constants.*;
import static org.webpieces.recorder.impl.TestCaseRecorder.RECORDER_KEY;

public class SchedulerImpl implements Scheduler {

    private static Logger log = LoggerFactory.getLogger(SchedulerImpl.class);

    private RemoteInvoker invoker;

    @Inject
    public SchedulerImpl(RemoteInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public XFuture<JobReference> schedule(Supplier<XFuture<Void>> runnable, long epochMsToRunAt) {
        ScheduleInfo info = new ScheduleInfo(epochMsToRunAt);
        return executeIt(runnable, info);
    }

    @Override
    public XFuture<JobReference> addToQueue(Supplier<XFuture<Void>> runnable) {
        ScheduleInfo info = new ScheduleInfo();
        return executeIt(runnable, info);
    }

    private XFuture<JobReference> executeIt(Supplier<XFuture<Void>> runnable, ScheduleInfo info) {
        Context.put(WEBPIECES_SCHEDULE_INFO, info);
        Context.put(WEBPIECES_SCHEDULE_RESPONSE, new JobReference());

        try {
            XFuture<Void> future = runnable.get();
            return future.thenApply(v -> {
                JobReference reference = Context.get(Constants.WEBPIECES_SCHEDULE_RESPONSE);
                TestCaseRecorder recorder = Context.get(RECORDER_KEY);
                if (recorder != null) {
                    EndpointInfo lastEndpointInfo = recorder.getLastEndpointInfo();
                    lastEndpointInfo.setJobRefId(reference.getTaskId());
                    lastEndpointInfo.setQueueApi(true);
                }
                log.info("executeIt reference " + reference.getTaskId());
                return reference;
            });
        } finally {
            Context.remove(WEBPIECES_SCHEDULE_INFO);
            Context.remove(WEBPIECES_SCHEDULE_RESPONSE);
        }
    }

    @Override
    public XFuture<Void> cancelJob(JobReference ref) {
        return invoker.delete(ref);
    }
}
