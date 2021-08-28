package org.webpieces.googlecloud.storage;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.googlecloud.storage.api.GCPRawStorage;
import org.webpieces.googlecloud.storage.impl.local.LocalStorage;

public class LocalOverrideModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(GCPRawStorage.class).to(LocalStorage.class).asEagerSingleton();
    }
}
