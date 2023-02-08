package org.webpieces.googlecloud.cloudtasks.localimpl;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.util.futures.XFuture;

public class LocalRemoteInvoker implements RemoteInvoker {

    @Override
    public XFuture<Void> invoke(String path, HttpMethod httpMethod, Object arg) {
        return null;
    }
}
