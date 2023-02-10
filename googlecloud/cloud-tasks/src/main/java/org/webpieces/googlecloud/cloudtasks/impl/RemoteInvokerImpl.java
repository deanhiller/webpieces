package org.webpieces.googlecloud.cloudtasks.impl;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.googlecloud.cloudtasks.api.ScheduleInfo;
import org.webpieces.util.futures.XFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class RemoteInvokerImpl implements RemoteInvoker {
    @Override
    public XFuture<Void> invoke(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info) {
        return null;
    }
}
