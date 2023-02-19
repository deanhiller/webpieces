package org.webpieces.googlecloud.cloudtasks.api;

import com.google.inject.ImplementedBy;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.impl.RemoteInvokerImpl;
import org.webpieces.util.futures.XFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@ImplementedBy(RemoteInvokerImpl.class)
public interface RemoteInvoker {

    public XFuture<Void> invoke(InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info);
}
