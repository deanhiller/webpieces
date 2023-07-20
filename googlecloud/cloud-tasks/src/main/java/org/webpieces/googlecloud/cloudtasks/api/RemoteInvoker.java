package org.webpieces.googlecloud.cloudtasks.api;

import com.google.inject.ImplementedBy;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.impl.RemoteInvokerImpl;
import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

@ImplementedBy(RemoteInvokerImpl.class)
public interface RemoteInvoker {

    XFuture<Void> invoke(Method method, HostWithPort addr, String path, HttpMethod httpMethod, String bodyAsText, ScheduleInfo info);

    XFuture<Void> delete(JobReference reference);

}
