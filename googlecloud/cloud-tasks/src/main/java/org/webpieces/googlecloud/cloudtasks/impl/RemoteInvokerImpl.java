package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.googlecloud.cloudtasks.localimpl.Endpoint;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.net.InetSocketAddress;
import java.security.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static javax.management.monitor.Monitor.scheduler;

public class RemoteInvokerImpl implements RemoteInvoker {

    @Inject
    private SchedulerFactory schedulerFactory;

    @Inject
    private GCPTaskClient gcpTasksClient;
    private Scheduler scheduler;

    public RemoteInvokerImpl(SchedulerFactory schedulerFactory, GCPTaskClient gcpTaskClient) {
        this.schedulerFactory = schedulerFactory;
        this.gcpTasksClient = gcpTaskClient;
    }

    @Override
    public XFuture<Void> invoke(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {

        if(scheduler==null) {
            scheduler = schedulerFactory.createScheduler();
        }

        JobReference ref = (JobReference) Context.get("webpieces-scheduleResponse");
        String jobId = UUID.randomUUID().toString();
        ref.setTaskId(jobId);

        //TODO: implement Map and map jobId to scheduled task to allow deletion of tasks as well.



        if(info.isScheduledInFuture()) {
            scheduler.schedule(
                    () -> gcpTasksClient.createTask(addr, httpMethod, path, bodyAsText),
                    info.getTime(), info.getTimeUnit());
        } else {
            //how to schedule
        }

        return null;
    }

    private void schedule(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText) {
        //Context.restoreContext(copy);
        Endpoint endpoint = new Endpoint(addr, httpMethod.toString(), path);
        XFuture<String> stringXFuture = client.sendHttpRequest(bodyAsText, endpoint);
        stringXFuture.exceptionally( (e) -> {
            log.error("Exception queueing the request", e);
            return null;
        });
        Context.clear();
    }
}
