package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class RemoteInvokerImpl implements RemoteInvoker {

    @Inject
    private GCPTaskClient gcpTasksClient;

    @Inject
    public RemoteInvokerImpl(GCPTaskClient gcpTaskClient) {
        this.gcpTasksClient = gcpTaskClient;
    }

    @Override
    public XFuture<Void> invoke(Method method, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {

        JobReference jobReference = gcpTasksClient.createTask(method, addr, httpMethod, path, bodyAsText, info);
        Context.put(Constants.WEBPIECES_SCHEDULE_RESPONSE,jobReference);

        return XFuture.completedFuture(null);
    }
}
