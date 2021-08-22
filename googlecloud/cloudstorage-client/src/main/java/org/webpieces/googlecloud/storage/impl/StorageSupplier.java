package org.webpieces.googlecloud.storage.impl;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.Serializable;
import java.util.function.Supplier;

public class StorageSupplier implements Supplier<Storage>, Serializable {

    @Override
    public Storage get() {
        return StorageOptions.newBuilder().build().getService();
    }

}
