package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

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
    public XFuture<Void> invoke(Method method, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info, QueueLookup lookup) {
        JobReference jobReference = gcpTasksClient.get().createTask(method, addr, httpMethod, path, bodyAsText, info, lookup);
        log.info("invoke jobReference "+jobReference);
        Context.put(Constants.WEBPIECES_SCHEDULE_RESPONSE, jobReference);
        return XFuture.completedFuture(null);
    }

    @Override
    public XFuture<Void> delete(JobReference reference) {
        this.gcpTasksClient.get().deleteTask(reference);
        return XFuture.completedFuture(null);
    }
}
