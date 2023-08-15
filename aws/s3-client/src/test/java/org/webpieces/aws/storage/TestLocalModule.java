package org.webpieces.aws.storage;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.webpieces.aws.storage.api.AWSRawStorage;
import org.webpieces.aws.storage.impl.local.LocalStorage;

public class TestLocalModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(AWSRawStorage.class).to(LocalStorage.class).asEagerSingleton();
    }

}
