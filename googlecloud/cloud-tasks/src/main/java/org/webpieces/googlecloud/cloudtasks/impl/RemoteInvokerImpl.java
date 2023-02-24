package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.net.InetSocketAddress;
import java.util.UUID;

public class RemoteInvokerImpl implements RemoteInvoker {

    @Inject
    private GCPTaskClient gcpTasksClient;

    @Inject
    public RemoteInvokerImpl(GCPTaskClient gcpTaskClient) {
        this.gcpTasksClient = gcpTaskClient;
    }

    @Override
    public XFuture<Void> invoke(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {

        /*
        JobReference ref = (JobReference) Context.get("webpieces-scheduleResponse");
        String jobId = UUID.randomUUID().toString();
        ref.setTaskId(jobId);
        */

        //TODO: implement Map and map jobId to scheduled task to allow deletion of tasks as well.


        return XFuture.completedFuture(null);
    }
}
