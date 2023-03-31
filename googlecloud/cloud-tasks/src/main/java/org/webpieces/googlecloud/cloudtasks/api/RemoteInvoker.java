package org.webpieces.googlecloud.cloudtasks.api;

import com.google.inject.ImplementedBy;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.impl.RemoteInvokerAction;
import org.webpieces.googlecloud.cloudtasks.impl.RemoteInvokerImpl;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

@ImplementedBy(RemoteInvokerImpl.class)
public interface RemoteInvoker {

    public XFuture<Void> invoke(Method method, InetSocketAddress addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info);
}
