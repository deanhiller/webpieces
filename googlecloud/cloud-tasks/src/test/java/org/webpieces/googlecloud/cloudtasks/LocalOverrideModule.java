package org.webpieces.googlecloud.cloudtasks;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.googlecloud.cloudtasks.localimpl.LocalRemoteInvoker;

public class LocalOverrideModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(RemoteInvoker.class).to(LocalRemoteInvoker.class);
    }
}
