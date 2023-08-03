package org.webpieces.googlecloud.storage.impl.local;

import com.google.cloud.storage.Storage;
import org.webpieces.googlecloud.storage.api.CopyInterface;

public class LocalCopyWriter implements CopyInterface {
    private Storage.CopyRequest copyRequest;

    public LocalCopyWriter(Storage.CopyRequest copyRequest){


        this.copyRequest = copyRequest;
    }
}
