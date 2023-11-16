package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.cloud.tasks.v2.Task;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Provider;
import java.lang.reflect.Method;

public class RemoteInvokerImpl implements RemoteInvoker {

    private static Logger log = LoggerFactory.getLogger(RemoteInvokerImpl.class);

    private SingletonSupplier<GCPTaskClient> gcpTasksClient;

    @Inject
    public RemoteInvokerImpl(Provider<GCPTaskClient> gcpTaskClient)
    {
        //constructors should never connect to remote services such
        // that TestBasicStart.java keeps working
        //instead, always have a start/stop method for a lifecycle (instead of constructor and stop method)
        this.gcpTasksClient = new SingletonSupplier<>(() -> gcpTaskClient.get());
    }

    @Override
    public XFuture<Void> invoke(Method method, HostWithPort addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {
        Task task = gcpTasksClient.get().createTask(method, addr, httpMethod, path, bodyAsText, info);
        JobReference jobReference = Context.get(Constants.WEBPIECES_SCHEDULE_RESPONSE);
        jobReference.setTaskId(task.getName());
        log.info("invoke jobReference "+jobReference);
        return XFuture.completedFuture(null);
    }

    @Override
    public XFuture<Void> delete(JobReference reference) {
        this.gcpTasksClient.get().deleteTask(reference);
        return XFuture.completedFuture(null);
    }
}
