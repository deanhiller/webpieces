package org.webpieces.googlecloud.cloudtasks.api;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.util.futures.XFuture;

public interface RemoteInvoker {

    public XFuture<Void> invoke(String path, HttpMethod httpMethod, Object arg);
}
