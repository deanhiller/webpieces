package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executor;

public class RemoteInvokerImpl implements RemoteInvoker {

    @Inject
    private GCPTaskClient gcpTasksClient;
    private Executor executor;

    @Inject
    public RemoteInvokerImpl(GCPTaskClient gcpTaskClient, Executor executor) {
        this.gcpTasksClient = gcpTaskClient;
        this.executor = executor;
    }

    @Override
    public XFuture<Void> invoke(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {


        JobReference jobReference = gcpTasksClient.createTask(addr, httpMethod, path, bodyAsText, info);
        Context.put(SchedulerImpl.WEBPIECES_SCHEDULE_RESPONSE,jobReference);

        return XFuture.completedFuture(jobReference);
    }
}
