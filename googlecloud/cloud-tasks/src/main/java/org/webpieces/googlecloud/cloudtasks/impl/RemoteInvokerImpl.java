package org.webpieces.googlecloud.cloudtasks.impl;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.*;
import org.webpieces.googlecloud.cloudtasks.localimpl.LocalRemoteInvoker;
import org.webpieces.util.SingletonSupplier;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class RemoteInvokerImpl implements RemoteInvoker {

    private final Logger log = LoggerFactory.getLogger(RemoteInvokerImpl.class);

    private SingletonSupplier<GCPTaskClient> gcpTasksClient;

    @Inject
    public RemoteInvokerImpl(Provider<GCPTaskClient> gcpTaskClient)
    {
        // constructors should never connect to remote services such
        // that TestBasicStart.java keeps working
        // instead, always have a start/stop method for a lifecycle (instead of constructor and stop method)
        this.gcpTasksClient = new SingletonSupplier<>(() -> gcpTaskClient.get());
    }

    @Override
    public XFuture<Void> invoke(Method method, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {
        RemoteInvokerAction action = Context.get(Constants.WEBPIECES_SCHEDULE_ACTION);

        if(action == null) {
            return XFuture.completedFuture(null);
        }

        switch (action){
            case GCP_CREATE_TASK: {
                JobReference jobReference = gcpTasksClient.get().createTask(method, addr, httpMethod, path, bodyAsText, info);
                Context.put(Constants.WEBPIECES_SCHEDULE_RESPONSE, jobReference);
                break;
            }
            case GCP_DELETE_TASK: {
                String taskId = Context.get(Constants.WEBPIECES_SCHEDULE_DELETE_TASK);
                log.info("taskId "+taskId);
                gcpTasksClient.get().deleteTask(method, taskId);
                break;
            }
            default:
                log.info("this log should not appear !!!!!!");
        }

        return XFuture.completedFuture(null);
    }
}
