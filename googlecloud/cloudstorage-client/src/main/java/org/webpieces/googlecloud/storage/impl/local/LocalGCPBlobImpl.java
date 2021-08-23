package org.webpieces.googlecloud.storage.impl.local;

import org.webpieces.googlecloud.storage.api.GCPBlob;

public class LocalGCPBlobImpl implements GCPBlob {
    String bucket;
    String name;

    public LocalGCPBlobImpl(String bucket, String name) {
        this.bucket = bucket;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
